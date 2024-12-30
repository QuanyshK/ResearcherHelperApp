import google.generativeai as genai
from django.conf import settings

class GeminiService:
    def __init__(self):
        genai.configure(api_key=settings.GEMINI_API_KEY)
        self.model = genai.GenerativeModel(settings.GEMINI_MODEL_NAME)

    def summarize(self, text):
        try:
            response = self.model.generate_content(f"Provide a detailed summary (a minimum 400-500 words) of the following text or article:: {text}")
            return response.text
        except Exception as e:
            return f"Error: {str(e)}"
