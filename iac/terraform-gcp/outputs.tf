output "cloud_run_url" {
  value = google_cloud_run_v2_service.api.uri
}

output "cloudsql_public_ip" {
  value = google_sql_database_instance.postgres.public_ip_address
}
