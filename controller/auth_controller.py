from flask import request, render_template, redirect, url_for, flash, current_app
from flask_login import login_user, logout_user, login_required
from flask_bcrypt import Bcrypt
from flask_mail import Mail, Message
from model import User, ResetToken
from middleware import validate_registration_data, validate_login_data, validate_password_reset_data

class AuthController:
    def __init__(self, bcrypt, mail):
        self.bcrypt = bcrypt
        self.mail = mail

    def register(self):
        """Handle user registration"""
        if request.method == 'POST':
            username = request.form.get('username')
            email = request.form.get('email')
            password = request.form.get('password')
            confirm_password = request.form.get('confirm_password')

            # Validate input data
            errors = validate_registration_data(username, email, password, confirm_password)
            if errors:
                for error in errors:
                    flash(error, 'error')
                return render_template('auth/register.html')

            # Check if user already exists
            if User.get_by_username(username):
                flash('Username already exists.', 'error')
                return render_template('auth/register.html')

            if User.get_by_email(email):
                flash('Email already registered.', 'error')
                return render_template('auth/register.html')

            # Create new user
            password_hash = self.bcrypt.generate_password_hash(password).decode('utf-8')
            user = User.create_user(username, email, password_hash)
            
            login_user(user)
            flash('Registration successful!', 'success')
            return redirect(url_for('blog.index'))

        return render_template('auth/register.html')

    def login(self):
        """Handle user login"""
        if request.method == 'POST':
            username = request.form.get('username')
            password = request.form.get('password')
            remember = bool(request.form.get('remember'))

            # Validate input data
            errors = validate_login_data(username, password)
            if errors:
                for error in errors:
                    flash(error, 'error')
                return render_template('auth/login.html')

            user = User.get_by_username(username)
            if user and self.bcrypt.check_password_hash(user.password_hash, password):
                login_user(user, remember=remember)
                next_page = request.args.get('next')
                flash('Login successful!', 'success')
                return redirect(next_page) if next_page else redirect(url_for('blog.index'))
            else:
                flash('Invalid username or password.', 'error')

        return render_template('auth/login.html')

    @login_required
    def logout(self):
        """Handle user logout"""
        logout_user()
        flash('You have been logged out.', 'info')
        return redirect(url_for('blog.index'))

    def forgot_password(self):
        """Handle forgot password request"""
        if request.method == 'POST':
            email = request.form.get('email')
            user = User.get_by_email(email)
            
            if user:
                # Generate reset token
                token = ResetToken.create_token(user.id)
                
                # Send reset email
                if current_app.config['MAIL_USERNAME']:
                    try:
                        msg = Message(
                            'Password Reset Request - Duffin\'s Blog',
                            sender=current_app.config['MAIL_USERNAME'],
                            recipients=[email]
                        )
                        reset_url = url_for('auth.reset_password', token=token, _external=True)
                        msg.body = f'''To reset your password, visit the following link:
{reset_url}

If you did not make this request, simply ignore this email and no changes will be made.

This link will expire in 1 hour.
'''
                        self.mail.send(msg)
                        flash('A password reset email has been sent.', 'info')
                    except Exception as e:
                        flash('Error sending email. Please try again later.', 'error')
                else:
                    flash('Email service not configured. Please contact administrator.', 'error')
            else:
                # Don't reveal whether the email exists
                flash('If that email address is in our system, you will receive a password reset email.', 'info')
        
        return render_template('auth/forgot_password.html')

    def reset_password(self, token):
        """Handle password reset"""
        reset_token = ResetToken.get_valid_token(token)
        
        if not reset_token:
            flash('Invalid or expired reset token.', 'error')
            return redirect(url_for('auth.forgot_password'))
        
        if request.method == 'POST':
            password = request.form.get('password')
            confirm_password = request.form.get('confirm_password')
            
            # Validate password data
            errors = validate_password_reset_data(password, confirm_password)
            if errors:
                for error in errors:
                    flash(error, 'error')
                return render_template('auth/reset_password.html', token=token)
            
            # Update password
            user = User.get(str(reset_token['user_id']))
            if user:
                password_hash = self.bcrypt.generate_password_hash(password).decode('utf-8')
                user.update_password(password_hash)
                
                # Delete the reset token
                ResetToken.delete_token(reset_token['_id'])
                
                flash('Your password has been updated!', 'success')
                return redirect(url_for('auth.login'))
        
        return render_template('auth/reset_password.html', token=token)
