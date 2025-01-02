from django.db import models

class Article(models.Model):
    doi = models.CharField(max_length=255, unique=True, null=True)
    hacked_link = models.URLField(blank=True, null=True)
    pdf_link = models.URLField(blank=True, null=True)

    def save(self, *args, **kwargs):
        self.hacked_link = f"https://sci-hub.ru/{self.doi}"
        super().save(*args, **kwargs)

    def __str__(self):
        return self.doi
