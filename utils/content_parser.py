import re

def parse_content_for_display(content):
    """Parse markdown-like content for HTML display"""
    # Basic image: ![alt text](image_url)
    content = re.sub(r'(?<!<img src=")\!\[(.*?)\]\((.*?)\)(?!">)', r'<img src="\2" alt="\1" class="img-fluid mb-2">', content)
    # Basic link: [link text](url)
    content = re.sub(r'\[(.*?)\]\((http[s]?://.*?)\)', r'<a href="\2" target="_blank" rel="noopener noreferrer">\1</a>', content)
    # Basic YouTube embed: [youtube](video_id_or_url)
    content = re.sub(r'\[youtube\]\((?:https?://)?(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/)?([a-zA-Z0-9_-]{11})\)', r'<div class="embed-responsive embed-responsive-16by9 mb-2"><iframe class="embed-responsive-item" src="https://www.youtube.com/embed/\1" allowfullscreen></iframe></div>', content)
    # Basic generic iframe embed: [embed](url)
    content = re.sub(r'\[embed\]\((http[s]?://.*?)\)', r'<div class="embed-responsive embed-responsive-16by9 mb-2"><iframe class="embed-responsive-item" src="\1" allowfullscreen></iframe></div>', content)
    # Bold text: **text**
    content = re.sub(r'\*\*(.*?)\*\*', r'<strong>\1</strong>', content)
    # Italic text: *text*
    content = re.sub(r'\*(.*?)\*', r'<em>\1</em>', content)
    # Strikethrough: ~~text~~
    content = re.sub(r'~~(.*?)~~', r'<del>\1</del>', content)
    # Code: `code`
    content = re.sub(r'`(.*?)`', r'<code>\1</code>', content)
    # Headers
    content = re.sub(r'^### (.*$)', r'<h3>\1</h3>', content, flags=re.MULTILINE)
    content = re.sub(r'^## (.*$)', r'<h2>\1</h2>', content, flags=re.MULTILINE)
    # Quote
    content = re.sub(r'^> (.*$)', r'<blockquote>\1</blockquote>', content, flags=re.MULTILINE)
    # List items
    content = re.sub(r'^- (.*$)', r'<li>\1</li>', content, flags=re.MULTILINE)
    # Newlines to br
    if not any(tag in content for tag in ['<pre', '<div', '<p']):
        content = content.replace('\n', '<br>')
    return content
