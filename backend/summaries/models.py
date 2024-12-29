from django.db import models

class Summary(models.Model):
    article_doi = models.CharField(max_length=200)
    text = models.TextField()
    timestamp = models.DateTimeField(auto_now_add=True)