import os
import re
import google.generativeai as genai
from config import Config

# Configure Gemini AI
if Config.GEMINI_API_KEY and Config.GEMINI_API_KEY != 'your-gemini-api-key-here':
    genai.configure(api_key=Config.GEMINI_API_KEY)
    gemini_model = genai.GenerativeModel('gemini-1.5-flash')
else:
    gemini_model = None
    print("Warning: GEMINI_API_KEY not configured. AI summaries will be disabled.")

def generate_ai_summary(content, title=""):
    """Generate an AI summary of the blog post content using Gemini AI"""
    if not gemini_model:
        return None
    
    try:
        # Clean the content and prepare prompt
        clean_content = content.replace('\n', ' ').strip()
        if len(clean_content) < 5:  # Too short to summarize
            return None
            
        # Adjust prompt based on content length
        if len(clean_content) < 50:
            prompt = f"""
            This is a very short blog post. Please provide a brief, thoughtful summary or interpretation in 1-2 sentences.
            Even if the content is minimal, try to provide some context or insight.
            
            Title: {title}
            Content: {clean_content}
            
            Summary:
            """
        else:
            prompt = f"""
            Please provide a concise and engaging summary of this blog post in 2-3 sentences. 
            Focus on the main points and key takeaways. The summary should be informative yet accessible.
            
            Title: {title}
            Content: {clean_content}
            
            Summary:
            """
        
        response = gemini_model.generate_content(prompt)
        if response and response.text:
            return response.text.strip()
    except Exception as e:
        print(f"Error generating AI summary: {e}")
        return None
    
    return None
