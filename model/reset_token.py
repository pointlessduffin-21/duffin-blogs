from datetime import datetime, timezone, timedelta
from bson import ObjectId
import secrets
from model.database import reset_tokens_collection

class ResetToken:
    @staticmethod
    def create_token(user_id):
        """Create a new reset token for a user"""
        token = secrets.token_urlsafe(32)
        reset_tokens_collection.insert_one({
            'user_id': ObjectId(user_id),
            'token': token,
            'created_at': datetime.now(timezone.utc),
            'expires_at': datetime.now(timezone.utc) + timedelta(hours=1)
        })
        return token
    
    @staticmethod
    def get_valid_token(token):
        """Get a valid (non-expired) reset token"""
        return reset_tokens_collection.find_one({
            'token': token,
            'expires_at': {'$gt': datetime.now(timezone.utc)}
        })
    
    @staticmethod
    def delete_token(token_id):
        """Delete a reset token"""
        reset_tokens_collection.delete_one({'_id': token_id})
