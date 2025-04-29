# Lettuce Crop Health Monitoring System

Agriculture faces significant challenges in plant health monitoring due to labor-intensive practices, limited knowledge, and human error. To address this, our research focuses specifically on **lettuce crops**, a widely cultivated plant with both economic and nutritional value, important for small-scale and industrial farming alike.

## Project Overview

This study proposes an innovative, **image-based system** for monitoring the health of lettuce crops using **computer vision**. Farmers often encounter difficulties in maintaining lettuce health, particularly in inspecting for weeds and diseases. Traditional agricultural management methods can be enhanced by technology such as computer vision systems, improving both **productivity** and **efficiency**.

## System Details

- **Model**: Utilizes the **YOLOv8** object detection model.
- **Dataset**: Trained on a **custom-annotated dataset** containing images of healthy and unhealthy lettuce samples.
- **Techniques**: 
  - **Data augmentation** and **image preprocessing** are applied to boost model performance and accuracy.
  - Enhancements ensure robustness against various environmental factors.

## Performance

- Achieved up to **90% detection accuracy** at a lower IoU threshold.
- Generalization performance reached approximately **60%** across different detection criteria.

## Application

To maximize accessibility and usability:
- The fine-tuned model is **integrated into an Android application**.
- Users can **upload lettuce images** directly from their mobile devices.
- The system analyzes whether the lettuce is **healthy**, **diseased**, or **weed-infested**.

## Model Training Repo
- <https://github.com/MarkYuriPG/plant_health_monitoring>

## Conclusion

This project highlights the potential of **AI-based** and **computer vision** solutions to **enhance plant health monitoring**, paving the way for improved agricultural accuracy and operational efficiency.
