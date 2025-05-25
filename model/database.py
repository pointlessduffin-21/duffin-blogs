from pymongo import MongoClient
from config import Config

# MongoDB setup
Config.validate_config()

client = MongoClient(Config.MONGO_URI)
db = client['duffins_blog']

# Collections
posts_collection = db['posts']
users_collection = db['users']
reset_tokens_collection = db['reset_tokens']
