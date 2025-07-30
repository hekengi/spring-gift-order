package gift.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KakaoMessageRequestDto {
    
    @JsonProperty("object_type")
    private String objectType;

    private Content content;
    private Link link;
    public KakaoMessageRequestDto() {}
    public KakaoMessageRequestDto(String objectType, Content content, Link link) {
        this.objectType = objectType;
        this.content = content;
        this.link = link;
    }
    
    // Content 내부 클래스
    public static class Content {
        private String title;
        private String description;
        
        @JsonProperty("image_url")
        private String imageUrl;
        
        @JsonProperty("image_width")
        private Integer imageWidth;
        
        @JsonProperty("image_height")
        private Integer imageHeight;
        
        private Link link;
        
        public Content() {}
        
        public Content(String title, String description, String imageUrl,
                      Integer imageWidth, Integer imageHeight, Link link) {
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.link = link;
        }
        
        // Getter & Setter
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public Integer getImageWidth() { return imageWidth; }
        public void setImageWidth(Integer imageWidth) { this.imageWidth = imageWidth; }
        
        public Integer getImageHeight() { return imageHeight; }
        public void setImageHeight(Integer imageHeight) { this.imageHeight = imageHeight; }
        
        public Link getLink() { return link; }
        public void setLink(Link link) { this.link = link; }
    }
    
    // Link 내부 클래스
    public static class Link {
        @JsonProperty("web_url")
        private String webUrl;
        
        @JsonProperty("mobile_web_url")
        private String mobileWebUrl;
        
        public Link() {}
        
        public Link(String webUrl, String mobileWebUrl) {
            this.webUrl = webUrl;
            this.mobileWebUrl = mobileWebUrl;
        }
        
        // Getter & Setter
        public String getWebUrl() { return webUrl; }
        public void setWebUrl(String webUrl) { this.webUrl = webUrl; }
        
        public String getMobileWebUrl() { return mobileWebUrl; }
        public void setMobileWebUrl(String mobileWebUrl) { this.mobileWebUrl = mobileWebUrl; }
    }
    
    // Getter & Setter
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    
    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }

    public Link getLink() { return link; }
    public void setLink(Link link) { this.link = link; }
} 