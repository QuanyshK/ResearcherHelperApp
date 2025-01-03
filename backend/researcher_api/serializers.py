from rest_framework import serializers
from .models import Article

class ArticleSerializer(serializers.ModelSerializer):
    class Meta:
        model = Article
        fields = ('id', 'doi', 'title', 'hacked_link', 'pdf_link', 'created_at')
