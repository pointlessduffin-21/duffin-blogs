import os
from flask import Flask, Blueprint
from flask_login import LoginManager
from flask_bcrypt import Bcrypt
from flask_mail import Mail
from datetime import datetime, timezone
from config import Config
from model import User

# Create Flask app
app = Flask(__name__)
app.config.from_object(Config)

# Create upload folder if it doesn't exist
os.makedirs(Config.UPLOAD_FOLDER, exist_ok=True)

# Initialize extensions
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'auth.login'
login_manager.login_message = 'Please log in to access this page.'
login_manager.login_message_category = 'info'

bcrypt = Bcrypt(app)
mail = Mail(app)

@login_manager.user_loader
def load_user(user_id):
    return User.get(user_id)

# Template context processor to provide datetime to all templates
@app.context_processor
def inject_datetime():
    return {'datetime': datetime, 'timezone': timezone}

# Import and register blueprints
from controller import AuthController, BlogController, APIController

# Initialize controllers
auth_controller = AuthController(bcrypt, mail)
blog_controller = BlogController()
api_controller = APIController()

# Create blueprints
auth_bp = Blueprint('auth', __name__, url_prefix='/auth')
blog_bp = Blueprint('blog', __name__)
api_bp = Blueprint('api', __name__, url_prefix='/api')

# Authentication routes
auth_bp.add_url_rule('/register', 'register', auth_controller.register, methods=['GET', 'POST'])
auth_bp.add_url_rule('/login', 'login', auth_controller.login, methods=['GET', 'POST'])
auth_bp.add_url_rule('/logout', 'logout', auth_controller.logout, methods=['POST'])
auth_bp.add_url_rule('/forgot_password', 'forgot_password', auth_controller.forgot_password, methods=['GET', 'POST'])
auth_bp.add_url_rule('/reset_password/<token>', 'reset_password', auth_controller.reset_password, methods=['GET', 'POST'])

# Blog routes
blog_bp.add_url_rule('/', 'index', blog_controller.index)
blog_bp.add_url_rule('/post/<slug>', 'view_post', blog_controller.view_post)
blog_bp.add_url_rule('/create', 'create_post_page', blog_controller.create_post_page, methods=['GET', 'POST'])
blog_bp.add_url_rule('/edit/<slug>', 'edit_post_page', blog_controller.edit_post_page, methods=['GET', 'POST'])
blog_bp.add_url_rule('/post/<slug>/delete', 'delete_post', blog_controller.delete_post, methods=['POST'])
blog_bp.add_url_rule('/upload_image', 'upload_image_for_editor', blog_controller.upload_image_for_editor, methods=['POST'])
blog_bp.add_url_rule('/api/generate-summary/<slug>', 'generate_summary_api', blog_controller.generate_summary_api)

# API routes  
api_bp.add_url_rule('/posts', 'get_posts_api', api_controller.get_posts_api, methods=['GET'])
api_bp.add_url_rule('/posts/<slug>', 'get_post_api', api_controller.get_post_api, methods=['GET'])
api_bp.add_url_rule('/posts', 'create_post_api', api_controller.create_post_api, methods=['POST'])
api_bp.add_url_rule('/posts/<slug>', 'update_post_api', api_controller.update_post_api, methods=['PUT'])
api_bp.add_url_rule('/posts/<slug>', 'delete_post_api', api_controller.delete_post_api, methods=['DELETE'])
api_bp.add_url_rule('/generate-summary/<slug>', 'generate_summary_api', api_controller.generate_summary_api, methods=['GET'])

# Register blueprints
app.register_blueprint(auth_bp)
app.register_blueprint(blog_bp)
app.register_blueprint(api_bp)

# Legacy route redirects for backward compatibility
@app.route('/register', methods=['GET', 'POST'])
def register():
    return auth_controller.register()

@app.route('/login', methods=['GET', 'POST'])
def login():
    return auth_controller.login()

@app.route('/logout')
def logout():
    return auth_controller.logout()

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
