from flask_login import UserMixin
from datetime import datetime, timezone
from bson import ObjectId
from model.database import users_collection

class User(UserMixin):
    def __init__(self, user_data):
        self.id = str(user_data['_id'])
        self.username = user_data['username']
        self.email = user_data['email']
        self.password_hash = user_data['password_hash']
        self.created_at = user_data.get('created_at', datetime.now(timezone.utc))
        self.is_active_user = user_data.get('is_active', True)

    def is_active(self):
        return self.is_active_user

    @staticmethod
    def get(user_id):
        """Get user by ID"""
        user_data = users_collection.find_one({"_id": ObjectId(user_id)})
        if user_data:
            return User(user_data)
        return None

    @staticmethod
    def get_by_username(username):
        """Get user by username"""
        user_data = users_collection.find_one({"username": username})
        if user_data:
            return User(user_data)
        return None

    @staticmethod
    def get_by_email(email):
        """Get user by email"""
        user_data = users_collection.find_one({"email": email})
        if user_data:
            return User(user_data)
        return None
    
    @staticmethod
    def create_user(username, email, password_hash):
        """Create a new user"""
        user_data = {
            'username': username,
            'email': email,
            'password_hash': password_hash,
            'created_at': datetime.now(timezone.utc),
            'is_active': True
        }
        result = users_collection.insert_one(user_data)
        return User.get(result.inserted_id)
    
    def update_password(self, password_hash):
        """Update user password"""
        users_collection.update_one(
            {'_id': ObjectId(self.id)},
            {'$set': {'password_hash': password_hash}}
        )
