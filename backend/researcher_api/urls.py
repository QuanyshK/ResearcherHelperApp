from django.urls import path
from .views import SciHubView

urlpatterns = [
    path('scihub/', SciHubView.as_view(), name='scihub-download'),
]