package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanyDto {

    public static class Create {
        @NotBlank(message = "Company name is required")
        @Size(min = 1, max = 150)
        private String name;

        private String industry = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }
    }

    public static class Response {
        private Long id;
        private String name;
        private String industry;

        public Response(Long id, String name, String industry) {
            this.id = id;
            this.name = name;
            this.industry = industry;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getIndustry() {
            return industry;
        }
    }
}
