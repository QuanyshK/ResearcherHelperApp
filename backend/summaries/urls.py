from django.urls import path
from .views import ChatListView, ChatCreateView, ChatDetailView, ChatDeleteView

urlpatterns = [
    path('chats/', ChatListView.as_view(), name='chat-list'),
    path('chats/create/', ChatCreateView.as_view(), name='chat-create'),
    path('chats/<int:pk>/', ChatDetailView.as_view(), name='chat-detail'),
    path('chats/<int:pk>/delete/', ChatDeleteView.as_view(), name='chat-delete'),
]
