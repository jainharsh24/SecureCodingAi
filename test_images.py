# Script to verify image existence and test docx styles
import os
import docx

images = [
    '5-attempt-history.png',
    'Activity_diagram.png',
    'Analyticspage.png',
    'Architecture.png',
    'challengespage.png',
    'Code-editorpage.png',
    'DFD_0_Diagram.drawio.png',
    'DFD_1_Diagram.drawio.png',
    'DFD_2_Diagram.drawio.png',
    'Existing Platform Comparison.png',
    'gann chart.png',
    'Homepage.png',
    'Learninghubpage.png',
    'Submission_analysis_page_1.png',
    'Submission_analysis_page_2.png',
    'UseCase_diagram.png',
    'Vuln chart.png'
]

missing = [img for img in images if not os.path.exists(os.path.join('images_&_ppt', img))]
print('Missing images:', missing)
print('All 17 images present!' if not missing else 'Some missing')
