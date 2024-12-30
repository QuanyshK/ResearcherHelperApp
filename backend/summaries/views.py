from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.views import APIView
from django.contrib.auth import get_user_model
from .models import ChatMessage
from .serializers import ChatMessageSerializer, ChatCreateSerializer
from .services import GeminiService
from users.models import User

class ChatMessageListView(generics.ListAPIView):
    serializer_class = ChatMessageSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ChatMessage.objects.filter(user=self.request.user).order_by('-created_at')


class ChatMessageCreateView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request, *args, **kwargs):
        serializer = ChatCreateSerializer(data=request.data)
        
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        text = serializer.validated_data['message']
        gemini = GeminiService()
        
        try:
            summary = gemini.summarize(text)
            
            if "Error:" in summary:
                return Response(
                    {"error": summary},
                    status=status.HTTP_503_SERVICE_UNAVAILABLE
                )

            chat = ChatMessage.objects.create(
                user=request.user,
                user_message=text,
                bot_response=summary
            )
            
            return Response(
                ChatMessageSerializer(chat).data,
                status=status.HTTP_201_CREATED
            )
        
        except Exception as e:
            return Response(
                {"error": f"Failed to save chat: {str(e)}"},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )


class ChatMessageDetailView(generics.RetrieveAPIView):
    serializer_class = ChatMessageSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ChatMessage.objects.filter(user=self.request.user)


class ChatMessageDeleteView(generics.DestroyAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ChatMessage.objects.filter(user=self.request.user)
