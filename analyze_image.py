import sys, os
from PIL import Image
from collections import Counter

def avg_color(img, box):
    crop = img.crop(box)
    px = list(crop.getdata())
    n = len(px)
    r = sum(p[0] for p in px)//n
    g = sum(p[1] for p in px)//n
    b = sum(p[2] for p in px)//n
    return (r,g,b)

def hexc(c): return "#%02X%02X%02X" % c

def dom_palette(img, samples=4000, buckets=12):
    w,h = img.size
    px = list(img.resize((min(w,200), min(h,200))).getdata())
    # quantize to 16-step buckets
    q = Counter((p[0]//16*16, p[1]//16*16, p[2]//16*16) for p in px)
    return [c for c,_ in q.most_common(buckets)]

def grid(img, cols=16, rows=9):
    w,h = img.size
    lines=[]
    for ry in range(rows):
        y0=ry*h//rows; y1=(ry+1)*h//rows
        row=[]
        for rx in range(cols):
            x0=rx*w//cols; x1=(rx+1)*w//cols
            row.append(hexc(avg_color(img,(x0,y0,x1,y1))))
        lines.append(" ".join(row))
    return lines

def sidebar_frac(img, thr=18):
    w,h=img.size
    left = avg_color(img,(0,0,4,h))
    for x in range(1, w):
        c = avg_color(img,(x,0,x+1,h))
        if abs(c[0]-left[0])+abs(c[1]-left[1])+abs(c[2]-left[2]) > thr:
            return x/w
    return 0.0

path = sys.argv[1]
img = Image.open(path).convert("RGB")
w,h = img.size
print("FILE: %s  SIZE: %dx%d  ratio=%.3f" % (os.path.basename(path), w, h, w/h))
print("sidebar_frac=%.3f  (~%dpx)" % (sidebar_frac(img), int(sidebar_frac(img)*w)))
print("SIDEBAR avg:", hexc(avg_color(img,(0,0,int(w*0.20),h))))
print("TOPBAR  avg:", hexc(avg_color(img,(int(w*0.20),0,w,int(h*0.07)))))
print("PLAYER  avg:", hexc(avg_color(img,(0,int(h*0.90),w,h))))
print("HERO    avg:", hexc(avg_color(img,(int(w*0.20),int(h*0.10),w,int(h*0.55)))))
print("CONTENT avg:", hexc(avg_color(img,(int(w*0.20),int(h*0.07),w,int(h*0.88)))))
print("DOMINANT:", " ".join(hexc(c) for c in dom_palette(img)))
print("GRID (16x9, left->right, top->bottom):")
for line in grid(img):
    print("  "+line)
print("-"*80)
