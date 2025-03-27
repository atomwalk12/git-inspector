commit 72c80faf6f2156b9dae063995d44410d29d7526d
Author: Razvan <atomwalk12@gmail.com>
Date:   Thu Mar 27 21:29:56 2025 +0100

    docs: update daily updates for Sprint 1 with detailed summaries and requirements
    
    - Revised summaries for daily updates on March 24 and 25 to include specific day references.
    - Expanded the March 26 update with a comprehensive workflow and architecture diagram.
    - Added a new March 27 update detailing requirements gathering, including a requirements table and explanations of requirement types.
    - Improved formatting in the requirements section for clarity and readability.

commit 16ec21a61cc775535449059b70259905039d5452
Author: Razvan <atomwalk12@gmail.com>
Date:   Thu Mar 27 21:28:42 2025 +0100

    feat(docs): enhance website layout with responsive tables and TOC
    
    - Updated the website's `hugo.yaml` to improve the resource table structure with a header and body for better readability.
    - Introduced new CSS styles for tables to support horizontal scrolling and improved aesthetics.
    - Added a new Table of Contents (TOC) feature to enhance navigation within documents.
    - Created a responsive table shortcode for easier integration in markdown files.
    - Removed outdated CSS rules and replaced them with new styles for better performance and maintainability.

commit 0f12bc191c8e2dc638b747fe7fd58b9b1b9f2224
Author: Razvan <atomwalk12@gmail.com>
Date:   Thu Mar 27 16:29:13 2025 +0100

    fix(github-actions): correct root file path for LaTeX compilation
    
    - Updated the root file path in the GitHub Actions workflows to reference the LaTeX document directly, ensuring proper compilation during the build process.

commit 8015cba51f6a7030a85f73322cd87f5356abcf7b
Author: Razvan <atomwalk12@gmail.com>
Date:   Thu Mar 27 16:25:38 2025 +0100

    fix(github-actions): set working directory for LaTeX compilation
    
    - Specified the working directory for the LaTeX document compilation step in the GitHub Actions workflow to ensure correct file paths during the build process.

commit 7779ee0b643372ebf82b63de28aa9f617f419731
Author: Razvan <atomwalk12@gmail.com>
Date:   Thu Mar 27 16:20:24 2025 +0100

    feat(github-actions): enhance CI workflow with LaTeX document compilation and artifact upload
    
    - Added steps to compile a LaTeX document and check for errors and warnings during the build process.
    - Implemented artifact upload for the generated PDF report.
    - Updated the pre-commit hook to re-add modified files after formatting, improving the commit process.

commit 5bb6f1eed30e3fdd84ce7c9595a548a5f7f1bba8
Author: Razvan <atomwalk12@gmail.com>
Date:   Thu Mar 27 16:16:42 2025 +0100

    docs: add LaTeX templates and bibliography for CVPR submissions
    
    - Introduced new LaTeX style files (`cvpr_eso.sty`, `cvpr.sty`, `eso-pic.sty`) for CVPR formatting.
    - Added a modified IEEE bibliography style (`ieee_fullname.bst`) to display full author names.
    - Included sample bibliography entries in `egbib.bib`.
    - Created a README file with instructions for compiling CVPR papers.
    - Added a sample report template (`report.tex`) for CVPR submissions.
    - Updated website content to reflect project resources and links.
