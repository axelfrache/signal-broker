#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import time
import os
from dotenv import load_dotenv
from kafka import KafkaProducer
from kafka.errors import KafkaError

load_dotenv()

KAFKA_BOOTSTRAP_SERVER = os.getenv('KAFKA_BOOTSTRAP_SERVER')
KAFKA_BOOTSTRAP_SERVERS = [KAFKA_BOOTSTRAP_SERVER]
MAIL_TOPIC = 'MailMessage'
WHATSAPP_TOPIC = 'WhatsappMessage'

def create_producer():
    """Crée et retourne un producer Kafka"""
    try:
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda v: json.dumps(v).encode('utf-8'),
            acks='all',
            retries=3
        )
        return producer
    except Exception as e:
        print(f"Erreur de connexion à Kafka: {e}")
        raise

def send_mail_messages(producer, mail_file='mail.json'):
    """Envoie les messages email à la queue Kafka"""
    with open(mail_file, 'r', encoding='utf-8') as f:
        messages = json.load(f)
    
    print(f"Envoi de {len(messages)} emails vers '{MAIL_TOPIC}'...")
    
    for i, message in enumerate(messages, 1):
        try:
            future = producer.send(MAIL_TOPIC, value=message)
            record_metadata = future.get(timeout=10)
            print(f"  [{i}/{len(messages)}] Email envoyé (offset: {record_metadata.offset})")
            time.sleep(0.5)
        except KafkaError as e:
            print(f"Erreur email {i}: {e}")

def send_whatsapp_messages(producer, whatsapp_file='whatsapp.json'):
    """Envoie les messages WhatsApp à la queue Kafka"""
    with open(whatsapp_file, 'r', encoding='utf-8') as f:
        messages = json.load(f)
    
    print(f"\nEnvoi de {len(messages)} messages WhatsApp vers '{WHATSAPP_TOPIC}'...")
    
    for i, message in enumerate(messages, 1):
        try:
            future = producer.send(WHATSAPP_TOPIC, value=message)
            record_metadata = future.get(timeout=10)
            print(f"  [{i}/{len(messages)}] WhatsApp envoyé (offset: {record_metadata.offset})")
            time.sleep(0.5)
        except KafkaError as e:
            print(f"Erreur WhatsApp {i}: {e}")

def main():
    """Fonction principale"""
    print(f"Kafka Sender - {KAFKA_BOOTSTRAP_SERVERS[0]}")
    
    try:
        producer = create_producer()
        send_mail_messages(producer)
        send_whatsapp_messages(producer)
        
        producer.flush()
        print("\n✓ Terminé")
        
    except Exception as e:
        print(f"\nErreur: {e}")
    finally:
        if 'producer' in locals():
            producer.close()

if __name__ == "__main__":
    main()
