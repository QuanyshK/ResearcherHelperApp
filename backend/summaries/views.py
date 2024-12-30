from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.views import APIView
from django.contrib.auth import get_user_model
from .models import Chat
from .serializers import ChatSerializer, ChatCreateSerializer
from .services import GeminiService

User = get_user_model()

class ChatListView(generics.ListAPIView):
    serializer_class = ChatSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Chat.objects.filter(user=self.request.user).order_by('-timestamp')

class ChatCreateView(APIView):
    permission_classes = [permissions.AllowAny]

    def post(self, request, *args, **kwargs):
        serializer = ChatCreateSerializer(data=request.data)
        if serializer.is_valid():
            text = serializer.validated_data['message']
            gemini = GeminiService()
            summary = gemini.summarize(text)

            if "Error:" in summary:
                return Response({"error": summary}, status=status.HTTP_503_SERVICE_UNAVAILABLE)

            try:
                chat = Chat.objects.create(
                    user=request.user,
                    message=text,
                    response=summary,
                    is_summary=True
                )
                return Response(ChatSerializer(chat).data, status=status.HTTP_201_CREATED)
            except Exception as e:
                return Response({"error": f"Failed to save chat: {str(e)}"}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class ChatDetailView(generics.RetrieveAPIView):
    serializer_class = ChatSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Chat.objects.filter(user=self.request.user)

class ChatDeleteView(generics.DestroyAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Chat.objects.filter(user=self.request.user)
