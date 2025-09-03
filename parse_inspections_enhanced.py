#!/usr/bin/env python3
"""
Enhanced IntelliJ Inspection XML to JSON Converter
Processes all XML files and creates categorized, prioritized JSON output
"""

import os
import json
import xml.etree.ElementTree as ET
from pathlib import Path
from dataclasses import dataclass, asdict
from typing import List, Dict, Optional
from collections import defaultdict

@dataclass
class InspectionProblem:
    file_path: str
    line: int
    severity: str
    inspection_id: str
    description: str
    language: str
    category: str
    priority: str
    fix_complexity: str
    highlighted_element: Optional[str] = None
    hints: List[str] = None

class InspectionAnalyzer:
    SEVERITY_MAPPING = {
        'ERROR': 'CRITICAL',
        'WARNING': 'HIGH',
        'WEAK WARNING': 'MEDIUM',
        'INFO': 'LOW',
        'INFORMATION': 'LOW'
    }
    
    PRIORITY_RULES = {
        # Security & Critical Issues
        'VulnerableLibrariesLocal': ('CRITICAL', 'SECURITY', 'HIGH'),
        'DataFlowIssue': ('CRITICAL', 'SECURITY', 'MEDIUM'),
        'NullableProblems': ('CRITICAL', 'RELIABILITY', 'MEDIUM'),
        'AutoCloseableResource': ('HIGH', 'RESOURCE', 'LOW'),
        
        # Code Quality
        'unused': ('HIGH', 'CLEANUP', 'LOW'),
        'UNUSED_IMPORT': ('HIGH', 'CLEANUP', 'LOW'),
        'LombokGetterMayBeUsed': ('MEDIUM', 'OPTIMIZATION', 'LOW'),
        'RedundantMethodOverride': ('MEDIUM', 'CLEANUP', 'LOW'),
        'SameParameterValue': ('MEDIUM', 'OPTIMIZATION', 'MEDIUM'),
        
        # Documentation & Style
        'Annotator': ('LOW', 'DOCUMENTATION', 'LOW'),
        'SpellCheckingInspection': ('LOW', 'DOCUMENTATION', 'LOW'),
        'MarkdownUnresolvedHeaderReference': ('LOW', 'DOCUMENTATION', 'LOW'),
        
        # Configuration & Properties
        'UnusedProperty': ('MEDIUM', 'CONFIGURATION', 'LOW'),
        'InconsistentResourceBundle': ('MEDIUM', 'CONFIGURATION', 'MEDIUM'),
        
        # JavaScript/Frontend
        'JSUnresolvedReference': ('MEDIUM', 'FRONTEND', 'MEDIUM'),
        'JSUnusedGlobalSymbols': ('MEDIUM', 'FRONTEND', 'LOW'),
    }

    def __init__(self, xml_directory: str):
        self.xml_directory = Path(xml_directory)
        self.problems: List[InspectionProblem] = []
        self.statistics = defaultdict(int)

    def parse_xml_file(self, xml_file: Path) -> List[InspectionProblem]:
        """Parse single XML file and extract problems"""
        try:
            tree = ET.parse(xml_file)
            root = tree.getroot()
            
            inspection_id = xml_file.stem
            problems = []
            
            for problem_elem in root.findall('.//problem'):
                file_elem = problem_elem.find('file')
                if file_elem is None:
                    continue
                    
                file_path = file_elem.text.replace('file://$PROJECT_DIR$/', '')
                
                # Extract basic info
                line = int(problem_elem.find('line').text) if problem_elem.find('line') is not None else 0
                
                problem_class = problem_elem.find('problem_class')
                severity = problem_class.get('severity', 'WARNING') if problem_class is not None else 'WARNING'
                
                description_elem = problem_elem.find('description')
                description = description_elem.text if description_elem is not None else 'No description'
                
                language_elem = problem_elem.find('language')
                language = language_elem.text if language_elem is not None else 'UNKNOWN'
                
                highlighted_elem = problem_elem.find('highlighted_element')
                highlighted_element = highlighted_elem.text if highlighted_elem is not None else None
                
                # Extract hints
                hints = []
                hints_elem = problem_elem.find('hints')
                if hints_elem is not None:
                    hints = [hint.get('value') for hint in hints_elem.findall('hint')]
                
                # Categorize and prioritize
                priority_info = self.PRIORITY_RULES.get(inspection_id, ('MEDIUM', 'GENERAL', 'MEDIUM'))
                priority, category, fix_complexity = priority_info
                
                # Override priority based on severity
                if severity == 'ERROR':
                    priority = 'CRITICAL'
                
                problem = InspectionProblem(
                    file_path=file_path,
                    line=line,
                    severity=severity,
                    inspection_id=inspection_id,
                    description=description,
                    language=language,
                    category=category,
                    priority=priority,
                    fix_complexity=fix_complexity,
                    highlighted_element=highlighted_element,
                    hints=hints or []
                )
                
                problems.append(problem)
                self.statistics[f"{inspection_id}_{severity}"] += 1
                self.statistics[f"priority_{priority}"] += 1
                self.statistics[f"category_{category}"] += 1
                
            return problems
            
        except ET.ParseError as e:
            print(f"Parse error in {xml_file}: {e}")
            return []
        except Exception as e:
            print(f"Error processing {xml_file}: {e}")
            return []

    def parse_all_files(self):
        """Parse all XML files in directory"""
        xml_files = list(self.xml_directory.glob("*.xml"))
        
        for xml_file in xml_files:
            if xml_file.name.startswith('.'):  # Skip hidden files like .descriptions.xml
                continue
                
            print(f"Processing {xml_file.name}...")
            problems = self.parse_xml_file(xml_file)
            self.problems.extend(problems)
            
        print(f"\nProcessed {len(xml_files)} files, found {len(self.problems)} problems")

    def generate_summary(self) -> Dict:
        """Generate analysis summary"""
        summary = {
            'total_problems': len(self.problems),
            'by_priority': {
                'CRITICAL': len([p for p in self.problems if p.priority == 'CRITICAL']),
                'HIGH': len([p for p in self.problems if p.priority == 'HIGH']),
                'MEDIUM': len([p for p in self.problems if p.priority == 'MEDIUM']),
                'LOW': len([p for p in self.problems if p.priority == 'LOW'])
            },
            'by_category': {},
            'by_language': {},
            'by_file': {},
            'top_issues': {}
        }
        
        # Category breakdown
        categories = defaultdict(int)
        languages = defaultdict(int) 
        files = defaultdict(int)
        inspections = defaultdict(int)
        
        for problem in self.problems:
            categories[problem.category] += 1
            languages[problem.language] += 1
            files[problem.file_path] += 1
            inspections[problem.inspection_id] += 1
            
        summary['by_category'] = dict(categories)
        summary['by_language'] = dict(languages)
        summary['by_file'] = dict(sorted(files.items(), key=lambda x: x[1], reverse=True)[:20])
        summary['top_issues'] = dict(sorted(inspections.items(), key=lambda x: x[1], reverse=True)[:15])
        
        return summary

    def create_fix_batches(self) -> Dict:
        """Create batches of related fixes that can be handled together"""
        batches = {
            'CRITICAL_SECURITY': [p for p in self.problems if p.priority == 'CRITICAL' and p.category == 'SECURITY'],
            'CRITICAL_RELIABILITY': [p for p in self.problems if p.priority == 'CRITICAL' and p.category == 'RELIABILITY'],
            'HIGH_CLEANUP': [p for p in self.problems if p.priority == 'HIGH' and p.category == 'CLEANUP'],
            'MEDIUM_OPTIMIZATION': [p for p in self.problems if p.priority == 'MEDIUM' and p.category == 'OPTIMIZATION'],
            'MEDIUM_CONFIGURATION': [p for p in self.problems if p.priority == 'MEDIUM' and p.category == 'CONFIGURATION'],
            'LOW_DOCUMENTATION': [p for p in self.problems if p.priority == 'LOW' and p.category == 'DOCUMENTATION']
        }
        
        return {name: [asdict(p) for p in problems] for name, problems in batches.items() if problems}

    def export_to_json(self, output_file: str):
        """Export analysis to JSON"""
        output = {
            'inspection_summary': self.generate_summary(),
            'fix_batches': self.create_fix_batches(),
            'all_problems': [asdict(p) for p in sorted(self.problems, key=lambda x: (x.priority, x.category, x.file_path))]
        }
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(output, f, indent=2, ensure_ascii=False)
        
        print(f"Analysis exported to {output_file}")
        return output

def main():
    xml_dir = "/Users/gimtaehui/IdeaProjects/board-hole/xml"
    output_file = "/Users/gimtaehui/IdeaProjects/board-hole/inspection_analysis.json"
    
    analyzer = InspectionAnalyzer(xml_dir)
    analyzer.parse_all_files()
    
    # Export to JSON
    analysis = analyzer.export_to_json(output_file)
    
    # Print summary
    summary = analysis['inspection_summary']
    print("\n" + "="*60)
    print("📊 INSPECTION SUMMARY")
    print("="*60)
    print(f"Total Problems: {summary['total_problems']}")
    print("\nBy Priority:")
    for priority, count in summary['by_priority'].items():
        if count > 0:
            emoji = {'CRITICAL': '🔴', 'HIGH': '🟡', 'MEDIUM': '🟢', 'LOW': '⚪'}[priority]
            print(f"  {emoji} {priority}: {count}")
    
    print("\nBy Category:")
    for category, count in sorted(summary['by_category'].items(), key=lambda x: x[1], reverse=True):
        print(f"  • {category}: {count}")
    
    print("\nTop Issues:")
    for issue, count in list(summary['top_issues'].items())[:10]:
        print(f"  • {issue}: {count}")
    
    print("\n📋 Fix Batches Created:")
    for batch_name, problems in analysis['fix_batches'].items():
        print(f"  • {batch_name}: {len(problems)} issues")

if __name__ == "__main__":
    main()