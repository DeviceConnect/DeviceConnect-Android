@echo off 

if "%1" == "build" (
    if not exist node_modules npm install
    if not exist dist npm run build
)

if "%1" == "clean" (
    if exist dist rmdir /s /q dist
)