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
        raise


def to_raw_event(source_type, source_msg_id, contact, body, original_date, metadata=None):
    if not original_date.endswith("Z") and "+" not in original_date:
        original_date = original_date + "Z"
    return {
        "eventId": str(uuid.uuid4()),
        "channelType": source_type,
        "sourceMessageId": source_msg_id,
        "timestamp": original_date,
        "contact": contact,
        "body": body,
        "metadata": metadata or {},
        "schemaVersion": 1,
    }


def send_mail_messages(producer, mail_file="mail.json"):
    with open(mail_file, "r", encoding="utf-8") as f:
        messages = json.load(f)

    for i, msg in enumerate(messages, 1):
        event = to_raw_event(
            source_type="MAIL",
            source_msg_id=f"mail-{i}-{uuid.uuid4().hex[:8]}",
            contact=msg["mail"],
            body=msg["message"],
            original_date=msg["date"],
        )
        try:
            future = producer.send(MAIL_TOPIC, value=event)
            record_metadata = future.get(timeout=10)
            time.sleep(0.5)
        except KafkaError as e:
            pass


def send_whatsapp_messages(producer, whatsapp_file="whatsapp.json"):
    with open(whatsapp_file, "r", encoding="utf-8") as f:
        messages = json.load(f)

    for i, msg in enumerate(messages, 1):
        event = to_raw_event(
            source_type="WHATSAPP",
            source_msg_id=f"wa-{i}-{uuid.uuid4().hex[:8]}",
            contact=str(msg["telephone"]),
            body=msg["message"],
            original_date=msg["date"],
        )
        try:
            future = producer.send(WHATSAPP_TOPIC, value=event)
            record_metadata = future.get(timeout=10)
            time.sleep(0.5)
        except KafkaError as e:
            pass


def send_malformed_messages(producer):
    try:
        with open("mail_malformed.json", "r", encoding="utf-8") as f:
            mail_messages = json.load(f)
        
        for i, msg in enumerate(mail_messages, 1):
            event = to_raw_event(
                source_type="MAIL",
                source_msg_id=f"malformed-mail-{i}-{uuid.uuid4().hex[:8]}",
                contact=msg["mail"],
                body=msg["message"],
                original_date=msg["date"],
            )
            try:
                future = producer.send(MAIL_TOPIC, value=event)
                record_metadata = future.get(timeout=10)
                time.sleep(0.5)
            except KafkaError as e:
                pass
    except FileNotFoundError:
        pass
    
    try:
        with open("whatsapp_malformed.json", "r", encoding="utf-8") as f:
            wa_messages = json.load(f)
        
        for i, msg in enumerate(wa_messages, 1):
            event = to_raw_event(
                source_type="WHATSAPP",
                source_msg_id=f"malformed-wa-{i}-{uuid.uuid4().hex[:8]}",
                contact=str(msg["telephone"]),
                body=msg["message"],
                original_date=msg["date"],
            )
            try:
                future = producer.send(WHATSAPP_TOPIC, value=event)
                record_metadata = future.get(timeout=10)
                time.sleep(0.5)
            except KafkaError as e:
                pass
    except FileNotFoundError:
        pass


def main():
    import sys
    
    send_malformed = "--malformed" in sys.argv or "-m" in sys.argv

    try:
        producer = create_producer()
        
        if send_malformed:
            send_malformed_messages(producer)
        else:
            send_mail_messages(producer)
            send_whatsapp_messages(producer)

        producer.flush()

    except Exception as e:
        pass
    finally:
        if "producer" in locals():
            producer.close()


if __name__ == "__main__":
    main()
