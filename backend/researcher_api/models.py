from django.db import models

class Article(models.Model):
    doi = models.CharField(max_length=200, unique=True)
    title = models.CharField(max_length=1000, null=True, blank=True)
    authors = models.TextField(null=True, blank=True)
    publisher = models.CharField(max_length=250, null=True, blank=True)
    url = models.URLField(max_length=1000, null=True, blank=True)