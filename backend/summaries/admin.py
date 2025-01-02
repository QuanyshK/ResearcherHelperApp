from django.contrib import admin
from .models import ChatMessage

@admin.register(ChatMessage)
class ChatMessageAdmin(admin.ModelAdmin):
    list_display = ('id', 'user', 'short_user_message', 'short_bot_response', 'file_name', 'created_at')
    search_fields = ('user__username', 'user_message')
    list_filter = ('created_at',)
    ordering = ('-created_at',)
    list_per_page = 50

    def short_user_message(self, obj):
        return obj.user_message[:50] + ('...' if len(obj.user_message) > 50 else '')
    def short_bot_response(self, obj):
        return obj.bot_response[:50] + ('...' if len(obj.bot_response) > 50 else '')


    short_user_message.short_description = "Message"
