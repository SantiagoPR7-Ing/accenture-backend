variable "project_id" { type = string }
variable "region"     { type = string  default = "us-central1" }

variable "db_name"     { type = string default = "accenture" }
variable "db_user"     { type = string default = "postgres" }
variable "db_password" { type = string sensitive = true }

# imagen que vas a subir a Artifact Registry
# ejemplo:
# us-central1-docker.pkg.dev/TU_PROJECT_ID/accenture-repo/accenture-api:latest
variable "image" { type = string }
