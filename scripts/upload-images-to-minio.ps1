# upload-images-to-minio.ps1
# Upload images to MinIO script
# @author Trae AI
# @date 2025-12-30

# Configuration
$MINIO_ALIAS = "myminio"
$MINIO_URL = "http://localhost:9000"
$ACCESS_KEY = "minioadmin"
$SECRET_KEY = "minioadmin123"
$BUCKET_NAME = "mall-files"
# Image source directory (relative to script location)
$IMAGE_DIR = "../frontend/public/images"

# Check if mc command exists
if (-not (Get-Command "mc" -ErrorAction SilentlyContinue)) {
    Write-Host "Error: 'mc' (MinIO Client) command not found." -ForegroundColor Red
    Write-Host "Please install MinIO Client: https://min.io/docs/minio/linux/reference/minio-mc.html"
    Write-Host "Or download Windows binary: https://dl.min.io/client/mc/release/windows-amd64/mc.exe"
    exit 1
}

# Check if image directory exists
$localPath = Resolve-Path $IMAGE_DIR -ErrorAction SilentlyContinue
if (-not $localPath) {
    Write-Host "Error: Image directory does not exist: $IMAGE_DIR" -ForegroundColor Red
    exit 1
}

Write-Host "Configuring MinIO alias..."
mc alias set $MINIO_ALIAS $MINIO_URL $ACCESS_KEY $SECRET_KEY

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to configure MinIO alias." -ForegroundColor Red
    exit 1
}

Write-Host "Checking Bucket '$BUCKET_NAME'..."
# Check if bucket exists
$bucketExists = mc ls $MINIO_ALIAS | Select-String $BUCKET_NAME
if (-not $bucketExists) {
    Write-Host "Bucket does not exist, creating..."
    mc mb "$MINIO_ALIAS/$BUCKET_NAME"
    # Set public policy
    mc anonymous set download "$MINIO_ALIAS/$BUCKET_NAME"
}
else {
    Write-Host "Bucket already exists."
}

Write-Host "Uploading images..."
# Recursively upload all files to images/ directory
# Target: myminio/mall-files/images/
$targetPath = "$MINIO_ALIAS/$BUCKET_NAME/images/"

# Get all files
$files = Get-ChildItem -Path $localPath -File -Recurse

foreach ($file in $files) {
    # Calculate relative path to preserve structure
    $relativePath = $file.FullName.Substring($localPath.Path.Length + 1)
    # Replace backslash with forward slash
    $relativePath = $relativePath -replace "\\", "/"
    
    $dest = "$targetPath$relativePath"
    
    Write-Host "Uploading: $relativePath -> $dest"
    mc cp $file.FullName $dest
}

Write-Host "Upload completed!" -ForegroundColor Green
