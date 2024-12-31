from rest_framework import serializers
from .models import ChatMessage

class ChatMessageSerializer(serializers.ModelSerializer):
    class Meta:
        model = ChatMessage
        fields = ['id', 'user', 'user_message', 'bot_response', 'created_at']


class ChatCreateSerializer(serializers.Serializer):
    message = serializers.CharField()
    file = serializers.FileField(required=False)