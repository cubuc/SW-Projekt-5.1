@echo off

rem TeX
latex Pflichtenheft > make.log
echo Okay (1)
latex Pflichtenheft > make.log
echo Okay (2)
latex Pflichtenheft > make.log
echo Okay (3)

echo Convert: DVI -> PS
dvips Pflichtenheft

echo Convert: PS -> PDF
ps2pdf Pflichtenheft.ps Pflichtenheft.pdf

echo Show PDF with Standard-Viewer
Pflichtenheft.pdf

rem Delete Litter
del *.log
del *.aux
del *.out
del *.toc
del *.ps?
del *.dvi
del PDF
del PS

rem Delete BackUps
rem del #*#
rem del *~
