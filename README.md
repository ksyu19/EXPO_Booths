# EXPO_Booths

Speed up booth-labeling process for career fair planning (Designed for the UT Austin Engineering Career EXPO).
See the example_output folder for sample results.

## User Guide

### Prepare booth maps:
If maps are .pdf files, convert them to image files (.jpg, .png).
You can convert the files through various online tools such as https://pdf2jpg.net/

### Prepare two .csv files which contain the information needed to label the booths:

**File 1: Company Info**
Company Name | FirstDay | SecondDay | Both | Booths
-------------|----------|-----------|------|-------
First company name | YES | NO | NO | 101
Second company name | NO | YES | NO | 102
Third company name | NO | NO | YES | 303
Fourth company name | YES | NO | NO | 404;405

The Company Info file must have the "Company Name" and "Booths" columns. If a company has two booths, the booths
must be separated by a semi-colon. Booths do not have to be integer values (ex. 355B and string values are ok).
The "FirstDay", "SecondDay", and "Both" columns are optional, and they are used to indicate whether the company
is attending the fair on the first day only, second day only, or both days. If these columns are included, a given
company should only have "YES" in one of the three columns (not case-sensitive).

**File 2: Booth Pixels**
Floor | Booths | X Pixel | Y Pixel
------|--------|---------|--------
Arena | 101 | 467 | 295
Arena | 102 | 400 | 330
Concourse | 303 | 185 | 785
Mezzanine | 404 | 200 | 975
Mezzanine | 405 | 205 | 1005

The Booth Pixels file must have all four of the above columns. Floor values can only be "Arena", "Concourse", or
"Mezzanine" (not case-sensitive). The floors should correspond with the map files. Booths should correspond with the
booths in the Company Info file. X and Y pixels are the desired locations for the top-left corner of each label. These
can be determined using programs such as Paint.
The included Booth_Pixels.csv file already has pixel information for each booth in the included map .jpg files.

For both files, the specific names of the column headers are unimportant. Additional columns are also allowed.

### Run the application:

1. Download the EXPO_Booths.jar file (in out/artifacts/EXPO_Booths_jar folder), and put it in the same folder as the
.csv and image files for easy file access.
1. Run the program by double-clicking on the file (make sure you have java).
![Application Example Image](/example_output/app_example_screenshot.png)
1. Select the correct .csv files. The input boxes below each file specify 1) how many rows to skip for column headers,
and 2) the column indices for each field. The indices start at 0.
1. Press the "Add Map Files" button as many times as needed. Choose the correct floors and corresponding maps.
1. Choose the day of the fair to map and press the "Label" button. Repeat to map the other day, if necessary.
