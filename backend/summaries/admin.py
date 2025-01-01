from django.contrib import admin
from .models import ChatMessage

@admin.register(ChatMessage)
class ChatMessageAdmin(admin.ModelAdmin):
    list_display = ('id', 'user', 'short_user_message', 'short_bot_response', 'file_name', 'created_at')
    list_filter = ('created_at', 'user')
    search_fields = ('user_message', 'bot_response', 'file_name')
    ordering = ('-created_at',)

    def short_user_message(self, obj):
        return obj.user_message[:50] + ('...' if len(obj.user_message) > 50 else '')

    def short_bot_response(self, obj):
        return obj.bot_response[:50] + ('...' if len(obj.bot_response) > 50 else '')

    short_user_message.short_description = "User Message"
    short_bot_response.short_description = "Bot Response"
