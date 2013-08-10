@echo off
del woolyfarm-latest.zip
"C:\Program Files\7-Zip\7z.exe" a -r -tzip woolyfarm-latest.zip woolyfarm/bin woolyfarm/libs woolyfarm-desktop/bin woolyfarm-desktop/libs runDesktop.bat
