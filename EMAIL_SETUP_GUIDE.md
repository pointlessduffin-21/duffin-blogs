# Email Configuration Guide for Duffin's Blog

## Overview
This guide helps you configure email functionality for the password reset feature in Duffin's Blog using your custom domain `@yeems214.xyz`.

## Current Configuration
The application is set up to use iCloud Mail with your custom domain. Here's what you need to do:

### Step 1: Generate App-Specific Password for iCloud

1. **Visit Apple ID Management**:
   - Go to [appleid.apple.com](https://appleid.apple.com)
   - Sign in with your Apple ID associated with your `@yeems214.xyz` domain

2. **Navigate to Security**:
   - Click on "Security" in the menu
   - Scroll down to "App-Specific Passwords"

3. **Generate New Password**:
   - Click "Generate Password..."
   - Enter a label: "Duffin's Blog Flask App"
   - Copy the generated 16-character password (format: xxxx-xxxx-xxxx-xxxx)

### Step 2: Update Environment Configuration

1. **Edit the `.env` file**:
   ```bash
   # Replace 'your-app-specific-password' with the password from Step 1
   MAIL_PASSWORD=your-generated-app-specific-password
   ```

2. **Verify other email settings**:
   ```bash
   MAIL_SERVER=smtp.mail.me.com
   MAIL_PORT=587
   MAIL_USE_TLS=True
   MAIL_USERNAME=duffin@yeems214.xyz
   ```

### Step 3: Test Email Configuration

1. **Start your Flask application**:
   ```bash
   python app.py
   ```

2. **Create an admin user** (if you haven't already):
   - Register with username 'admin'
   - This will allow you to access the test email endpoint

3. **Test email sending**:
   - Visit: `http://localhost:5001/test_email`
   - Check if you receive a test email
   - Remove this endpoint in production

### Step 4: Test Password Reset Flow

1. **Test the complete flow**:
   - Go to `/forgot_password`
   - Enter a registered user's email
   - Check email for reset link
   - Click the link and reset password

## Alternative Email Providers

### If using Gmail with custom domain:
```env
MAIL_SERVER=smtp.gmail.com
MAIL_PORT=587
MAIL_USE_TLS=True
MAIL_USERNAME=duffin@yeems214.xyz
MAIL_PASSWORD=your-app-specific-password
```

### If using Outlook with custom domain:
```env
MAIL_SERVER=smtp-mail.outlook.com
MAIL_PORT=587
MAIL_USE_TLS=True
MAIL_USERNAME=duffin@yeems214.xyz
MAIL_PASSWORD=your-password
```

### If using a custom SMTP server:
```env
MAIL_SERVER=smtp.yeems214.xyz
MAIL_PORT=587
MAIL_USE_TLS=True
MAIL_USERNAME=duffin@yeems214.xyz
MAIL_PASSWORD=your-password
```

## Troubleshooting

### Common Issues:

1. **"Authentication failed" error**:
   - Verify app-specific password is correct
   - Ensure 2FA is enabled on your Apple ID
   - Check that the email address exists in iCloud Mail

2. **"Connection refused" error**:
   - Verify MAIL_SERVER and MAIL_PORT settings
   - Check if your ISP blocks SMTP ports
   - Try using port 465 with SSL instead of TLS

3. **Email not received**:
   - Check spam/junk folders
   - Verify the recipient email exists in your user database
   - Test with the `/test_email` endpoint first

### Production Considerations:

1. **Remove test endpoint**:
   - Delete or comment out the `/test_email` route before deployment

2. **Enhanced email templates**:
   - Consider using HTML email templates for better formatting
   - Add your blog's branding to emails

3. **Email logging**:
   - Add proper logging for email send attempts
   - Monitor failed email deliveries

## Email Flow Security

The password reset system includes several security measures:

- **Secure tokens**: Generated using `secrets.token_urlsafe(32)`
- **Time-limited**: Tokens expire after 1 hour
- **Single-use**: Tokens are deleted after use
- **No user enumeration**: System doesn't reveal if email exists
- **Secure storage**: Tokens stored in MongoDB with expiration

## Next Steps

1. Configure your email credentials in `.env`
2. Test the email functionality
3. Test the complete password reset flow
4. Remove test endpoints before production deployment
5. Consider implementing HTML email templates for better UX

## Support

If you encounter issues:
1. Check the Flask application logs for detailed error messages
2. Verify your domain's email configuration with your provider
3. Test basic SMTP connectivity with tools like telnet or mail clients
