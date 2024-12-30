from django.urls import path
from .views import ChatMessageCreateView, ChatMessageListView, ChatMessageDetailView, ChatMessageDeleteView

urlpatterns = [
    path("send/", ChatMessageCreateView.as_view(), name="send_message"),
    path("", ChatMessageListView.as_view(), name="list_messages"),
    path("<int:pk>/", ChatMessageDetailView.as_view(), name="chat_detail"),
    path("<int:pk>/delete/", ChatMessageDeleteView.as_view(), name="chat_delete"),
]