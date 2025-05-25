import os
from werkzeug.utils import secure_filename
from config import Config

def allowed_file(filename):
    """Check if file extension is allowed"""
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in Config.ALLOWED_EXTENSIONS

def save_uploaded_file(file, filename_prefix=""):
    """Save uploaded file and return the URL"""
    if file and allowed_file(file.filename):
        filename = secure_filename(f"{filename_prefix}{file.filename}")
        file_path = os.path.join(Config.UPLOAD_FOLDER, filename)
        file.save(file_path)
        return f"uploads/{filename}"
    return None

def delete_file(file_path):
    """Delete a file if it exists"""
    try:
        full_path = os.path.join(Config.UPLOAD_FOLDER, os.path.basename(file_path))
        if os.path.exists(full_path):
            os.remove(full_path)
            return True
    except Exception as e:
        print(f"Error deleting file {file_path}: {e}")
    return False
