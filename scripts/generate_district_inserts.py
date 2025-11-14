import os, re, glob

BASE = 'src/main/java/com/sinloingok/app/constant'

output_lines = []
for path in glob.glob(f'{BASE}/**/*.java', recursive=True):
    with open(path, encoding='utf-8') as f:
        lines = f.readlines()
    class_name = os.path.splitext(os.path.basename(path))[0]
    class_comment = ''
    # capture first javadoc before interface definition
    for i,line in enumerate(lines):
        if line.strip().startswith('/**'):
            comm = []
            l=i
            while l < len(lines):
                comm_line = lines[l].strip().lstrip('/').lstrip('*').rstrip('*/').strip()
                if comm_line:
                    comm.append(comm_line)
                if lines[l].strip().endswith('*/'):
                    break
                l+=1
            class_comment = ''.join(comm)
            break
    for idx,line in enumerate(lines):
        m = re.search(r'public\s+(?:static\s+)?(?:final\s+)?\w+\s+([A-Za-z0-9_]+)\s*=\s*(.+);', line)
        if not m:
            continue
        var = m.group(1)
        raw_val = m.group(2).strip()
        if raw_val.startswith('"') and raw_val.endswith('"'):
            value = raw_val.strip('"')
        else:
            value = raw_val.rstrip('L')
        # find previous comment
        comment = ''
        p = idx-1
        while p>=0 and lines[p].strip()=='':
            p-=1
        if p>=0:
            if lines[p].strip().startswith('/**'):
                comm=[]
                k=p
                while k>=0:
                    cl=lines[k].strip().lstrip('/').lstrip('*').rstrip('*/').strip()
                    if cl:
                        comm.insert(0,cl)
                    if lines[k].strip().startswith('/**'):
                        break
                    k-=1
                comment=''.join(comm)
            elif lines[p].strip().startswith('//'):
                comment=lines[p].strip().lstrip('/').strip()
        description = ''
        if class_comment and comment:
            description = class_comment + '  ' + comment
        code = f'{class_name}{var}'
        line_sql = f"INSERT INTO district(code,name,value,description) VALUES ('{code}','{var}','{value}','{description}');"
        output_lines.append(line_sql)

with open('src/main/resources/sql/district_inserts.sql','w',encoding='utf-8') as out:
    out.write('\n'.join(output_lines))
