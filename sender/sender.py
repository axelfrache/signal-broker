#!/usr/bin/env python3

import json
import uuid
import time
import os
from datetime import datetime, timezone
from dotenv import load_dotenv
from kafka import KafkaProducer
from kafka.errors import KafkaError

load_dotenv()

KAFKA_BOOTSTRAP_SERVER = os.getenv("KAFKA_BOOTSTRAP_SERVER", "localhost:9092")
KAFKA_BOOTSTRAP_SERVERS = [KAFKA_BOOTSTRAP_SERVER]
MAIL_TOPIC = os.getenv("KAFKA_MAIL_TOPIC", "support.mail.raw")
WHATSAPP_TOPIC = os.getenv("KAFKA_WHATSAPP_TOPIC", "support.whatsapp.raw")


def create_producer():
    try:
        return KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda v: json.dumps(v).encode("utf-8"),
            acks="all",
            retries=3,
        )
    except Exception as e:
        print(f"Erreur de connexion à Kafka: {e}")
        raise


def to_raw_event(source_type, source_msg_id, contact, body, metadata=None):
    return {
        "eventId": str(uuid.uuid4()),
        "sourceType": source_type,
        "sourceMessageId": source_msg_id,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "contact": contact,
        "body": body,
        "metadata": metadata or {},
        "schemaVersion": 1,
    }


def send_mail_messages(producer, mail_file="mail.json"):
    with open(mail_file, "r", encoding="utf-8") as f:
        messages = json.load(f)

    print(f"Envoi de {len(messages)} emails vers '{MAIL_TOPIC}'...")

    for i, msg in enumerate(messages, 1):
        event = to_raw_event(
            source_type="MAIL",
            source_msg_id=f"mail-{i}-{uuid.uuid4().hex[:8]}",
            contact=msg["mail"],
            body=msg["message"],
            metadata={"client": "sender-script", "originalDate": msg["date"]},
        )
        try:
            future = producer.send(MAIL_TOPIC, value=event)
            record_metadata = future.get(timeout=10)
            print(f"  [{i}/{len(messages)}] Email envoyé (offset: {record_metadata.offset})")
            time.sleep(0.5)
        except KafkaError as e:
            print(f"Erreur email {i}: {e}")


def send_whatsapp_messages(producer, whatsapp_file="whatsapp.json"):
    with open(whatsapp_file, "r", encoding="utf-8") as f:
        messages = json.load(f)

    print(f"\nEnvoi de {len(messages)} messages WhatsApp vers '{WHATSAPP_TOPIC}'...")

    for i, msg in enumerate(messages, 1):
        event = to_raw_event(
            source_type="WHATSAPP",
            source_msg_id=f"wa-{i}-{uuid.uuid4().hex[:8]}",
            contact=str(msg["telephone"]),
            body=msg["message"],
            metadata={"platform": "whatsapp", "originalDate": msg["date"]},
        )
        try:
            future = producer.send(WHATSAPP_TOPIC, value=event)
            record_metadata = future.get(timeout=10)
            print(f"  [{i}/{len(messages)}] WhatsApp envoyé (offset: {record_metadata.offset})")
            time.sleep(0.5)
        except KafkaError as e:
            print(f"Erreur WhatsApp {i}: {e}")


def main():
    print(f"Kafka Sender - {KAFKA_BOOTSTRAP_SERVERS[0]}")
    print(f"  Mail topic:     {MAIL_TOPIC}")
    print(f"  WhatsApp topic: {WHATSAPP_TOPIC}")

    try:
        producer = create_producer()
        send_mail_messages(producer)
        send_whatsapp_messages(producer)

        producer.flush()
        print("\n✓ Terminé")

    except Exception as e:
        print(f"\nErreur: {e}")
    finally:
        if "producer" in locals():
            producer.close()


if __name__ == "__main__":
    main()
