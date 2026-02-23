# Sender - Générateur de tickets Kafka

Ce programme envoie des tickets clients (emails et WhatsApp) vers des queues Kafka.

## Installation

```bash
pip install -r requirements.txt
```

## Configuration

Le sender se connecte à Kafka sur `localhost:9092` par défaut.

Topics utilisés :
- `MailMessage` - Pour les messages email
- `WhatsappMessage` - Pour les messages WhatsApp

## Utilisation

```bash
python sender.py
```

Le programme va :
1. Lire les fichiers `mail.json` et `whatsapp.json`
2. Envoyer chaque message un par un vers la queue Kafka correspondante
3. Afficher la progression en temps réel

## Fichiers JSON

### mail.json
```json
{
  "date": "2026-02-22T18:23:45.123456",
  "mail": "jean.dupont@gmail.com",
  "message": "...",
  "media": {...}
}
```

### whatsapp.json
```json
{
  "date": "2026-02-22T14:32:18.453621",
  "telephone": 677845923,
  "message": "...",
  "media": {...}
}
```
