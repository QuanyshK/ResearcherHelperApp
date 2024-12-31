from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework.parsers import MultiPartParser, FormParser, JSONParser
from django.contrib.auth import get_user_model

from .services import GeminiService
from .models import ChatMessage
from .serializers import ChatMessageSerializer, ChatCreateSerializer
from users.models import User
from PyPDF2 import PdfReader
from docx import Document
import os


def extract_text_from_pdf(file_path):
    text = ""
    with open(file_path, 'rb') as f:
        reader = PdfReader(f)
        for page in reader.pages:
            text += page.extract_text() or ''
    return text


def extract_text_from_docx(file_path):
    doc = Document(file_path)
    return "\n".join([para.text for para in doc.paragraphs])


class ChatMessageListView(generics.ListAPIView):
    serializer_class = ChatMessageSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ChatMessage.objects.filter(user=self.request.user).order_by('created_at')


class ChatMessageCreateView(APIView):
    permission_classes = [permissions.IsAuthenticated]
    parser_classes = [MultiPartParser, FormParser, JSONParser]

    def post(self, request, *args, **kwargs):
        serializer = ChatCreateSerializer(data=request.data)
        
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        text = serializer.validated_data.get('message', '')
        file_obj = request.FILES.get('file')
        prompt_instructions = ""
        
        if file_obj:
            filename = file_obj.name
            file_path = os.path.join('/tmp', filename)
            
            with open(file_path, 'wb+') as f:
                for chunk in file_obj.chunks():
                    f.write(chunk)

            if filename.lower().endswith('.pdf'):
                text = extract_text_from_pdf(file_path)

            elif filename.lower().endswith('.docx'):
                text = extract_text_from_docx(file_path)

            os.remove(file_path)
            if not text:
                return Response({"error": "Failed to extract text from file"}, status=status.HTTP_400_BAD_REQUEST)
        
        if not text:
            return Response({"error": "Text or File required"}, status=status.HTTP_400_BAD_REQUEST)

        gemini = GeminiService()
        try:
            summary = gemini.summarize(prompt_instructions + text) if prompt_instructions else gemini.summarize(text)
            
            if "Error:" in summary:
                return Response({"error": summary}, status=status.HTTP_503_SERVICE_UNAVAILABLE)

            chat = ChatMessage.objects.create(
                user=request.user,
                user_message=text,
                bot_response=summary
            )
            
            return Response(ChatMessageSerializer(chat).data, status=status.HTTP_201_CREATED)
        
        except Exception as e:
            return Response({"error": f"Failed to save chat: {str(e)}"}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class ChatMessageDetailView(generics.RetrieveAPIView):
    serializer_class = ChatMessageSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ChatMessage.objects.filter(user=self.request.user)


class ChatMessageDeleteView(generics.DestroyAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ChatMessage.objects.filter(user=self.request.user)
