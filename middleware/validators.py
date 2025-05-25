def validate_registration_data(username, email, password, confirm_password):
    """Validate user registration data"""
    errors = []
    
    if not all([username, email, password, confirm_password]):
        errors.append('All fields are required.')
    
    if password != confirm_password:
        errors.append('Passwords do not match.')
    
    if len(password) < 6:
        errors.append('Password must be at least 6 characters long.')
    
    # Basic email validation
    import re
    email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    if not re.match(email_pattern, email):
        errors.append('Please enter a valid email address.')
    
    # Username validation
    if len(username) < 3:
        errors.append('Username must be at least 3 characters long.')
    
    return errors

def validate_login_data(username, password):
    """Validate user login data"""
    errors = []
    
    if not username or not password:
        errors.append('Username and password are required.')
    
    return errors

def validate_post_data(title, content):
    """Validate blog post data"""
    errors = []
    
    if not title or not title.strip():
        errors.append('Title is required.')
    
    if len(title) > 200:
        errors.append('Title must be less than 200 characters.')
    
    return errors

def validate_password_reset_data(password, confirm_password):
    """Validate password reset data"""
    errors = []
    
    if not password or not confirm_password:
        errors.append('Both password fields are required.')
    
    if password != confirm_password:
        errors.append('Passwords do not match.')
    
    if len(password) < 6:
        errors.append('Password must be at least 6 characters long.')
    
    return errors
