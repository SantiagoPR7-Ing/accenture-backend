terraform {
  required_version = ">= 1.5.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

# Habilitar APIs necesarias
resource "google_project_service" "services" {
  for_each = toset([
    "run.googleapis.com",
    "sqladmin.googleapis.com",
    "artifactregistry.googleapis.com"
  ])
  service            = each.value
  disable_on_destroy = false
}

# Artifact Registry (Docker)
resource "google_artifact_registry_repository" "repo" {
  depends_on = [google_project_service.services]
  location   = var.region
  repository_id = "accenture-repo"
  format     = "DOCKER"
}

# Cloud SQL Postgres (PUBLIC IP abierta)
resource "google_sql_database_instance" "postgres" {
  depends_on       = [google_project_service.services]
  name             = "accenture-postgres"
  database_version = "POSTGRES_16"
  region           = var.region

  settings {
    tier = "db-f1-micro"

    ip_configuration {
      ipv4_enabled = true

      authorized_networks {
        name  = "open-to-world"
        value = "0.0.0.0/0"
      }
    }
  }

  deletion_protection = false
}

resource "google_sql_database" "db" {
  name     = var.db_name
  instance = google_sql_database_instance.postgres.name
}

resource "google_sql_user" "user" {
  name     = var.db_user
  instance = google_sql_database_instance.postgres.name
  password = var.db_password
}

# Cloud Run (v2)
resource "google_cloud_run_v2_service" "api" {
  depends_on = [google_project_service.services]
  name     = "accenture-api"
  location = var.region

  template {
    containers {
      image = var.image

      # Variables Spring (R2DBC) usando PUBLIC IP de Cloud SQL
      env {
        name  = "SPRING_R2DBC_URL"
        value = "r2dbc:postgresql://${google_sql_database_instance.postgres.public_ip_address}:5432/${var.db_name}"
      }
      env {
        name  = "SPRING_R2DBC_USERNAME"
        value = var.db_user
      }
      env {
        name  = "SPRING_R2DBC_PASSWORD"
        value = var.db_password
      }

      ports {
        container_port = 8080
      }
    }
  }

  ingress = "INGRESS_TRAFFIC_ALL"
}

# Permitir invocación pública
resource "google_cloud_run_v2_service_iam_member" "public_invoker" {
  name     = google_cloud_run_v2_service.api.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "allUsers"
}

