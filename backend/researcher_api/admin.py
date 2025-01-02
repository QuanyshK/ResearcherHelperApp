from django.contrib import admin
from .models import Article

@admin.register(Article)
class ArticleAdmin(admin.ModelAdmin):
    list_display = ('id', 'doi', 'hacked_link', 'pdf_link')
    search_field = ('doi')