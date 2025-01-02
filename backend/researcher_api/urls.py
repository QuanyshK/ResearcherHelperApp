from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import generate_scihub_link


urlpatterns = [
    path('generate-link/', generate_scihub_link, name='generate_link'),
]