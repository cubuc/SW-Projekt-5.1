@echo off

rem TeX
latex Pflichtenheft.tex > make.log
echo Okay (1)
latex Pflichtenheft.tex > make.log
echo Okay (2)
latex Pflichtenheft.tex > make.log
echo Okay (3)

rem Show DVI
yap Pflichtenheft.dvi



rem Delete BackUps
rem del #*#
rem del *~

rem Delete Litter
rem del Pflichtenheft.log
rem del Pflichtenheft.aux
rem del Pflichtenheft.toc
rem del Pflichtenheft.ilg

