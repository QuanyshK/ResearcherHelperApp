from django.db import models
from users.models import User

class ChatMessage(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="chat_messages")
    user_message = models.TextField()
    bot_response = models.TextField()
    file_name = models.CharField(max_length=255, blank=True, null=True)  
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"User: {self.file_name or self.user_message[:50]} - Bot: {self.bot_response[:50]}"
