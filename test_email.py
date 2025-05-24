#!/usr/bin/env python3

import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

def test_email_connection():
    print("Testing iCloud email configuration...")
    print(f"MAIL_SERVER: {os.getenv('MAIL_SERVER')}")
    print(f"MAIL_PORT: {os.getenv('MAIL_PORT')}")
    print(f"MAIL_USERNAME: {os.getenv('MAIL_USERNAME')}")
    print(f"MAIL_PASSWORD: {'*' * len(os.getenv('MAIL_PASSWORD', ''))} (length: {len(os.getenv('MAIL_PASSWORD', ''))})")
    
    # Test configurations to try
    configs = [
        {
            'server': 'smtp.mail.me.com',
            'port': 587,
            'username': os.getenv('MAIL_USERNAME'),
            'name': 'iCloud SMTP (TLS)'
        },
        {
            'server': 'smtp.mail.me.com',
            'port': 465,
            'username': os.getenv('MAIL_USERNAME'),
            'name': 'iCloud SMTP (SSL)',
            'use_ssl': True
        },
        {
            'server': 'smtp.gmail.com',
            'port': 587,
            'username': os.getenv('MAIL_USERNAME'),
            'name': 'Gmail SMTP (for comparison)'
        }
    ]
    
    for config in configs:
        print(f"\n--- Testing {config['name']} ---")
        try:
            # Create message
            msg = MIMEMultipart()
            msg['From'] = config['username']
            msg['To'] = "francisroelabarca14@gmail.com"
            msg['Subject'] = f"Test Email from Duffin's Blog - {config['name']}"
            
            body = f"This is a test email using {config['name']} to verify the email configuration."
            msg.attach(MIMEText(body, 'plain'))
            
            # Create SMTP session
            print(f"Connecting to {config['server']}:{config['port']}...")
            
            if config.get('use_ssl'):
                server = smtplib.SMTP_SSL(config['server'], config['port'])
            else:
                server = smtplib.SMTP(config['server'], config['port'])
                print("Starting TLS encryption...")
                server.starttls()  # Enable TLS encryption
            
            print("Logging in...")
            server.login(config['username'], os.getenv('MAIL_PASSWORD'))
            
            print("Sending email...")
            text = msg.as_string()
            server.sendmail(config['username'], "francisroelabarca14@gmail.com", text)
            
            print(f"✅ SUCCESS: Email sent successfully using {config['name']}!")
            server.quit()
            return True
            
        except Exception as e:
            print(f"❌ FAILED: {type(e).__name__}: {str(e)}")
            continue
    
    return False

def test_smtp_connection_only():
    """Test just the SMTP connection without sending email"""
    print("\n--- Testing SMTP Connection Only ---")
    try:
        server = smtplib.SMTP('smtp.mail.me.com', 587)
        server.starttls()
        server.login(os.getenv('MAIL_USERNAME'), os.getenv('MAIL_PASSWORD'))
        print("✅ SMTP Authentication successful!")
        server.quit()
        return True
    except Exception as e:
        print(f"❌ SMTP Authentication failed: {str(e)}")
        return False

if __name__ == "__main__":
    print("=" * 60)
    print("EMAIL CONFIGURATION TEST")
    print("=" * 60)
    
    # First test just authentication
    if test_smtp_connection_only():
        print("\n🎉 Authentication works! Now testing email sending...")
        test_email_connection()
    else:
        print("\n🔧 Authentication failed. Trying different configurations...")
        test_email_connection()
