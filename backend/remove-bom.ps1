# Remove BOM from Java files
Get-ChildItem -Path ".\src\main\java\com\campus\marketplace\controller" -Filter "*Controller.java" | ForEach-Object {
    $content = [System.IO.File]::ReadAllBytes($_.FullName)
    
    # Check if file starts with UTF-8 BOM (EF BB BF)
    if ($content.Length -ge 3 -and $content[0] -eq 0xEF -and $content[1] -eq 0xBB -and $content[2] -eq 0xBF) {
        Write-Host "Removing BOM from: $($_.Name)"
        # Remove first 3 bytes (BOM) and write back
        $newContent = $content[3..($content.Length - 1)]
        [System.IO.File]::WriteAllBytes($_.FullName, $newContent)
    }
}
Write-Host "Done!"
