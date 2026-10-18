(()=>{'use strict';
const $=s=>document.querySelector(s),viewport=$('#viewport'),itemsEl=$('#items'),lg=$('#lg'),world=$('#world'),canvas=$('#ink'),ctx=canvas.getContext('2d'),props=$('#props');
const LS='ideaspace-local-project-v3',COLORS=['yellow','pink','mint','blue','orange'],HEX={yellow:'#fff2a8',pink:'#ffc6d9',mint:'#c4f0d8',blue:'#bfdcff',orange:'#ffd6a5'},INKS=['#c7baff','#7de4ca','#f5c96a','#ff9db8','#ffffff'];
// كل نوع عنصر له أبعاده ونصه الإرشادي؛ لإضافة نوع جديد: أضف مدخلاً هنا + دالة بناء في render + أدواته في bar
const DEF={text:{w:280,h:56,ph:'اكتب نصاً…'},note:{w:220,h:180,ph:'اكتب ملاحظتك…'},shape:{w:170,h:120,ph:'نص'},mind:{w:170,h:56,ph:'فكرة'},image:{w:280,h:0,ph:''},file:{w:300,h:220,ph:''}};
const SHAPES={rect:'▭',round:'▢',ellipse:'⬭',diamond:'◇',tri:'△'};
let state={items:[],links:[],strokes:[]},view={x:0,y:0,scale:1},nextId=1,tool='pan',sel=null,cfrom=null,undoS=[],redoS=[],ink={color:INKS[0],width:3,erase:false},saveT,toastT,typed=false;
const els=new Map(),get=id=>state.items.find(o=>o.id===id);
/* ===== تخزين IndexedDB ===== */
const idb=new Promise((ok,no)=>{try{const r=indexedDB.open('ideaspace-db',1);r.onupgradeneeded=()=>r.result.createObjectStore('kv');r.onsuccess=()=>ok(r.result);r.onerror=()=>no(r.error)}catch(e){no(e)}});
const kv=(m,f)=>idb.then(db=>new Promise((ok,no)=>{const t=db.transaction('kv',m),q=f(t.objectStore('kv'));t.oncomplete=()=>ok(q.result);t.onerror=t.onabort=()=>no(t.error)}));
function queueSave(){const s=$('#saveStatus');s.textContent='● جارٍ الحفظ…';s.style.color='#f5c96a';clearTimeout(saveT);saveT=setTimeout(save,350)}
async function save(){const s=$('#saveStatus');try{await kv('readwrite',st=>st.put({...state,view,v:4},'project'));s.textContent='● محفوظ محلياً';s.style.color='#82e2c8'}catch(e){s.textContent='! تعذّر الحفظ';s.style.color='#ffb2a9';toast('تعذّر الحفظ (المساحة ممتلئة أو التخزين غير متاح). صدّر نسخة JSON الآن.')}}
addEventListener('pagehide',save);document.addEventListener('visibilitychange',()=>{if(document.hidden)save()});
/* ===== تراجع/إعادة (الصور تُخزَّن مرجعاً لا نسخاً) ===== */
const tk=new Map(),tv=[],tok=s=>{let t=tk.get(s);if(t===undefined){t='@@'+tv.length;tv.push(s);tk.set(s,t)}return t};
const snap=()=>JSON.stringify({items:state.items,links:state.links,strokes:state.strokes},(k,v)=>k==='src'&&typeof v==='string'?tok(v):v);
const unsnap=j=>JSON.parse(j,(k,v)=>k==='src'&&typeof v==='string'&&v.startsWith('@@')?tv[+v.slice(2)]:v);
const checkpoint=()=>{undoS.push(snap());if(undoS.length>60)undoS.shift();redoS=[]};
function step(a,b,msg){if(!a.length){toast('لا توجد خطوات');return}b.push(snap());restore(unsnap(a.pop()));queueSave();toast(msg)}
const undo=()=>step(undoS,redoS,'تم التراجع'),redo=()=>step(redoS,undoS,'تمت الإعادة');
function toast(m){const e=$('#toast');e.textContent=m;e.classList.add('show');clearTimeout(toastT);toastT=setTimeout(()=>e.classList.remove('show'),2400)}
/* ===== ترحيل البيانات (v3 → v4) دون حذف القديم ===== */
function migrate(d){const o={items:[],links:[],strokes:[]};for(const it of d.items||[]){const k=it.kind==='title'?'text':it.kind;if(!DEF[k])continue;const n={...it,kind:k,id:Number(it.id)};if(!n.w)n.w=DEF[k].w;if(!n.h&&DEF[k].h)n.h=DEF[k].h;if(k==='shape'&&!SHAPES[n.shape])n.shape='round';if(!HEX[n.color])n.color=k==='note'?'yellow':k==='mind'?'mint':'blue';if(k==='file'&&typeof n.text==='string')n.text=n.text.replace(/\\n/g,'\n');o.items.push(n)}
const ids=new Set(o.items.map(i=>i.id));o.links=(d.links||[]).filter(l=>ids.has(l.a)&&ids.has(l.b));o.strokes=(d.strokes||[]).filter(s=>Array.isArray(s.points));return o}
function restore(d){state=migrate(d);nextId=Math.max(0,...state.items.map(o=>o.id))+1;itemsEl.innerHTML='';els.clear();state.items.forEach(render);if(sel&&!get(sel))sel=null;renderLinks();drawAll();select(sel);$('#welcome').style.display=state.items.length?'none':''}
/* ===== اللوحة ===== */
const pt=(cx,cy)=>{const r=viewport.getBoundingClientRect();return{x:(cx-r.left-view.x)/view.scale,y:(cy-r.top-view.y)/view.scale}};
const center=()=>{const r=viewport.getBoundingClientRect();return pt(r.left+r.width/2,r.top+r.height/2)};
function setView(){world.style.transform=`translate(${view.x}px,${view.y}px) scale(${view.scale})`;$('#zoomLabel').textContent=Math.round(view.scale*100)+'%';drawAll();queueSave()}
function zoomAt(f,cx,cy,quiet){const r=viewport.getBoundingClientRect(),old=view.scale,s=Math.max(.1,Math.min(4,old*f));cx=cx??r.left+r.width/2;cy=cy??r.top+r.height/2;view.x=cx-r.left-(cx-r.left-view.x)*s/old;view.y=cy-r.top-(cy-r.top-view.y)*s/old;view.scale=s;if(!quiet)setView()}
function rect(o){const e=els.get(o.id);return{x:o.x,y:o.y,w:e?e.offsetWidth:o.w,h:e?e.offsetHeight:(o.h||60)}}
function bounds(){let a=1e9,b=1e9,c=-1e9,d=-1e9;const e=(x,y)=>{a=Math.min(a,x);b=Math.min(b,y);c=Math.max(c,x);d=Math.max(d,y)};state.items.forEach(o=>{const r=rect(o);e(r.x,r.y);e(r.x+r.w,r.y+r.h)});state.strokes.forEach(s=>s.points.forEach(p=>e(p.x,p.y)));return a>c?null:{x0:a,y0:b,x1:c,y1:d}}
function fit(){const b=bounds(),W=viewport.clientWidth,H=viewport.clientHeight;if(!b){view={x:W/2,y:H/2,scale:1};return setView()}const w=b.x1-b.x0||1,h=b.y1-b.y0||1,s=Math.min(1.2,Math.max(.1,Math.min(W/(w+120),H/(h+120))));view={scale:s,x:(W-w*s)/2-b.x0*s,y:(H-h*s)/2-b.y0*s};setView()}
function fitCanvas(){const d=Math.min(devicePixelRatio||1,1.5);canvas.width=Math.round(viewport.clientWidth*d);canvas.height=Math.round(viewport.clientHeight*d);canvas._d=d;drawAll()}
function drawAll(){const d=canvas._d||1;ctx.setTransform(1,0,0,1,0,0);ctx.clearRect(0,0,canvas.width,canvas.height);ctx.setTransform(view.scale*d,0,0,view.scale*d,view.x*d,view.y*d);ctx.lineCap=ctx.lineJoin='round';for(const s of state.strokes){if(!s.points.length)continue;ctx.strokeStyle=s.color;ctx.lineWidth=s.width;ctx.beginPath();s.points.forEach((p,i)=>i?ctx.lineTo(p.x,p.y):ctx.moveTo(p.x,p.y));if(s.points.length===1)ctx.lineTo(s.points[0].x+.01,s.points[0].y);ctx.stroke()}}
/* ===== رسم كل نوع بشكله الخاص ===== */
function shapeBody(o){const f=`fill="${HEX[o.color]||HEX.blue}" stroke="#0008" stroke-width="2" vector-effect="non-scaling-stroke"`;return({rect:`<rect x="2" y="2" width="96" height="96" ${f}/>`,round:`<rect x="2" y="2" width="96" height="96" rx="14" ${f}/>`,ellipse:`<ellipse cx="50" cy="50" rx="48" ry="48" ${f}/>`,diamond:`<polygon points="50,2 98,50 50,98 2,50" ${f}/>`,tri:`<polygon points="50,3 98,97 2,97" ${f}/>`})[o.shape||'round']}
function style(el,o){const s=el.style;s.left=o.x+'px';s.top=o.y+'px';s.width=o.w+'px';if(o.kind==='image')s.height='auto';else if(o.kind==='text'){s.height='auto';s.minHeight=(o.h||56)+'px';s.fontSize=(o.size||28)+'px'}else s.height=(o.h||DEF[o.kind].h)+'px';s.setProperty('--c',HEX[o.color]||HEX.yellow);el.dataset.lock=o.locked?1:0}
function render(o){const old=els.get(o.id),el=document.createElement('article');el.className='it it-'+o.kind;el.dataset.id=o.id;els.set(o.id,el);
if(o.kind==='shape')el.innerHTML=`<svg viewBox="0 0 100 100" preserveAspectRatio="none">${shapeBody(o)}</svg>`;
if(o.kind==='image'){const im=new Image();im.src=o.src;im.alt='';im.draggable=false;el.append(im)}
else if(o.kind==='file'){const b=document.createElement('b'),d=document.createElement('div');b.textContent='📄 '+(o.name||'ملف');d.textContent=o.text;el.append(b,d)}
else{const t=document.createElement('div');t.className='tx';t.contentEditable='false';t.dataset.ph=DEF[o.kind].ph;t.textContent=o.text||'';
t.addEventListener('input',()=>{if(!typed){checkpoint();typed=true}o.text=t.innerText.replace(/\n$/,'');queueSave()});
t.addEventListener('blur',()=>{t.contentEditable='false';el.classList.remove('editing');typed=false});
t.addEventListener('paste',e=>{e.preventDefault();document.execCommand('insertText',false,e.clipboardData.getData('text/plain'))});el.append(t)}
const rz=document.createElement('i');rz.className='rz';el.append(rz);style(el,o);old?old.replaceWith(el):itemsEl.append(el);return el}
function edit(el){const o=get(+el.dataset.id),t=el&&el.querySelector('.tx');if(!t||o.locked)return;el.classList.add('editing');t.contentEditable='true';t.focus();const r=document.createRange();r.selectNodeContents(t);r.collapse(false);const s=getSelection();s.removeAllRanges();s.addRange(r)}
function add(kind,x,y,extra={}){const d=DEF[kind],o={id:nextId++,kind,x:x-d.w/2,y:y-(d.h||80)/2,w:d.w,h:d.h||undefined,text:'',color:kind==='note'?COLORS[state.items.filter(i=>i.kind==='note').length%5]:kind==='mind'?'mint':kind==='shape'?'blue':'yellow',...extra};if(kind==='shape')o.shape=extra.shape||'round';state.items.push(o);render(o);$('#welcome').style.display='none';queueSave();return o}
function create(kind){const c=center(),j=(state.items.length%6)*22;checkpoint();const o=add(kind,c.x+j,c.y+j);select(o.id);edit(els.get(o.id))}
function child(p){checkpoint();const n=state.links.filter(l=>l.a===p.id&&l.tree).length,c=add('mind',p.x-155,p.y+n*72+28,{color:p.color});state.links.push({a:p.id,b:c.id,tree:1});renderLinks();select(c.id);edit(els.get(c.id))}
function refresh(o){render(o);if(sel===o.id)els.get(o.id).classList.add('selected');renderLinks();bar();queueSave()}
function del(o){state.items=state.items.filter(i=>i!==o);state.links=state.links.filter(l=>l.a!==o.id&&l.b!==o.id);els.get(o.id)?.remove();els.delete(o.id);sel=null;renderLinks();bar();queueSave();toast('حُذف العنصر')}
function select(id){sel=id;els.forEach((e,i)=>e.classList.toggle('selected',i===id));bar()}
/* ===== الروابط ===== */
function edge(r,tx,ty){const cx=r.x+r.w/2,cy=r.y+r.h/2,dx=tx-cx,dy=ty-cy;if(!dx&&!dy)return{x:cx,y:cy};const k=Math.min(r.w/2/(Math.abs(dx)||1e-9),r.h/2/(Math.abs(dy)||1e-9));return{x:cx+dx*k,y:cy+dy*k}}
function linkD(l){const a=get(l.a),b=get(l.b);if(!a||!b)return null;const ra=rect(a),rb=rect(b),p=edge(ra,rb.x+rb.w/2,rb.y+rb.h/2),q=edge(rb,ra.x+ra.w/2,ra.y+ra.h/2),dx=(q.x-p.x)*.4,dy=(q.y-p.y)*.4,hz=Math.abs(q.x-p.x)>Math.abs(q.y-p.y);return{tree:l.tree,col:l.tree?(HEX[a.color]||'#a99cff'):'#a99cff',d:hz?`M${p.x},${p.y}C${p.x+dx},${p.y} ${q.x-dx},${q.y} ${q.x},${q.y}`:`M${p.x},${p.y}C${p.x},${p.y+dy} ${q.x},${q.y-dy} ${q.x},${q.y}`}}
function renderLinks(){let h='';state.links.forEach((l,i)=>{const k=linkD(l);if(!k)return;h+=`<path d="${k.d}" fill="none" stroke="${k.col}" stroke-width="${k.tree?4:3}" stroke-linecap="round"${k.tree?'':' marker-end="url(#ah)"'}/><path d="${k.d}" data-l="${i}" fill="none" stroke="transparent" stroke-width="24" style="pointer-events:stroke"/>`});lg.innerHTML=h}
function connect(o){if(cfrom===null){cfrom=o.id;select(o.id);toast('اضغط العنصر الثاني للربط');return}if(cfrom!==o.id&&!state.links.some(l=>(l.a===cfrom&&l.b===o.id)||(l.a===o.id&&l.b===cfrom))){checkpoint();state.links.push({a:cfrom,b:o.id});renderLinks();queueSave();toast('تم الربط')}setTool('pan')}
/* ===== شريط الخصائص: يتغيّر حسب نوع العنصر ===== */
function bar(){const o=get(sel);
if(tool==='draw'){props.className='props';props.innerHTML=INKS.map(c=>`<button class="sw${ink.color===c&&!ink.erase?' on':''}" data-p="ink" data-v="${c}" style="--c:${c}"></button>`).join('')+`<span class="sep"></span><button data-p="wid">${ink.width>3?'●':'•'}</button><button data-p="erase" class="${ink.erase?'on':''}">⌫</button><button data-p="done">تم</button>`;return}
if(!o){props.className='props hidden';return}
const sw=COLORS.map(c=>`<button class="sw${o.color===c?' on':''}" data-p="color" data-v="${c}" style="--c:${HEX[c]}"></button>`).join('');let h='';
if(o.kind==='note')h=sw;
if(o.kind==='shape')h=Object.entries(SHAPES).map(([k,v])=>`<button class="${o.shape===k?'on':''}" data-p="shape" data-v="${k}">${v}</button>`).join('')+'<span class="sep"></span>'+sw;
if(o.kind==='text')h='<button data-p="size" data-v="-4">A−</button><button data-p="size" data-v="4">A+</button>';
if(o.kind==='mind')h='<button data-p="child">＋ فرع</button>'+sw;
h+=(h?'<span class="sep"></span>':'')+`<button data-p="link">↗</button><button data-p="dup">⧉</button><button data-p="front">⬆</button><button data-p="back">⬇</button><button data-p="lock">${o.locked?'🔓':'🔒'}</button><button data-p="del">🗑</button>`;props.className='props';props.innerHTML=h}
props.addEventListener('click',e=>{const b=e.target.closest('[data-p]');if(!b)return;const p=b.dataset.p,v=b.dataset.v;
if(p==='ink'){ink.color=v;ink.erase=false;return bar()}if(p==='wid'){ink.width=ink.width>3?3:6;return bar()}if(p==='erase'){ink.erase=!ink.erase;return bar()}if(p==='done')return setTool('pan');
const o=get(sel);if(!o)return;if(p==='lock'){checkpoint();o.locked=!o.locked;return refresh(o)}
if(o.locked){toast('العنصر مقفل — اضغط 🔓');return}if(p==='link'){cfrom=o.id;toast('اضغط العنصر الذي تريد الربط به');return}if(p==='child')return child(o);
checkpoint();
if(p==='color')o.color=v;else if(p==='shape')o.shape=v;else if(p==='size')o.size=Math.max(12,Math.min(160,(o.size||28)+ +v));
else if(p==='dup'){const n={...JSON.parse(JSON.stringify(o)),id:nextId++,x:o.x+30,y:o.y+30};state.items.push(n);render(n);select(n.id);return queueSave()}
else if(p==='front'){state.items.splice(state.items.indexOf(o),1);state.items.push(o);itemsEl.append(els.get(o.id));return queueSave()}
else if(p==='back'){state.items.splice(state.items.indexOf(o),1);state.items.unshift(o);itemsEl.prepend(els.get(o.id));return queueSave()}
else if(p==='del')return del(o);refresh(o)});
function setTool(t){tool=t;if(t!=='connect')cfrom=null;document.querySelectorAll('.toolbar [data-cmd]').forEach(b=>b.classList.toggle('active',b.dataset.cmd===t));viewport.style.cursor=t==='draw'?'crosshair':'';bar()}
/* ===== التفاعل: تحريك وتغيير حجم العناصر ===== */
function onMove(el,mv,up){const end=()=>{el.removeEventListener('pointermove',mv);el.removeEventListener('pointerup',end);el.removeEventListener('pointercancel',end);up()};el.addEventListener('pointermove',mv);el.addEventListener('pointerup',end);el.addEventListener('pointercancel',end)}
itemsEl.addEventListener('pointerdown',e=>{const el=e.target.closest('.it');if(!el||tool==='draw'||e.button>0)return;const o=get(+el.dataset.id);e.stopPropagation();
if(e.target.classList.contains('rz')){if(o.locked)return;checkpoint();el.setPointerCapture(e.pointerId);const p0=pt(e.clientX,e.clientY),w0=o.w,h0=o.h||el.offsetHeight;onMove(el,ev=>{const q=pt(ev.clientX,ev.clientY);o.w=Math.max(60,w0+q.x-p0.x);el.style.width=o.w+'px';if(o.kind!=='image'){o.h=Math.max(36,h0+q.y-p0.y);o.kind==='text'?el.style.minHeight=o.h+'px':el.style.height=o.h+'px'}renderLinks()},queueSave);return}
if(el.classList.contains('editing')&&e.target.closest('.tx'))return;
if(tool==='connect'||cfrom!==null){connect(o);return}
const was=sel===o.id;select(o.id);if(o.locked)return;const p0=pt(e.clientX,e.clientY),sx=o.x,sy=o.y;let moved=false,ck=false;el.setPointerCapture(e.pointerId);
onMove(el,ev=>{const q=pt(ev.clientX,ev.clientY),dx=q.x-p0.x,dy=q.y-p0.y;if(!moved&&Math.hypot(dx,dy)*view.scale<5)return;if(!ck){checkpoint();ck=true}moved=true;o.x=sx+dx;o.y=sy+dy;el.style.left=o.x+'px';el.style.top=o.y+'px';renderLinks()},()=>{if(moved)queueSave();else if(was)edit(el)})});
/* ===== تحريك اللوحة والرسم ===== */
const act=new Set();let pan=null,stroke=null,t0=null;
function eraseAt(p){const r=14/view.scale,n=state.strokes.length;state.strokes=state.strokes.filter(s=>!s.points.some(q=>Math.hypot(q.x-p.x,q.y-p.y)<r));if(state.strokes.length!==n){drawAll();queueSave()}}
viewport.addEventListener('pointerdown',e=>{if(e.target.closest('.it')&&tool!=='draw')return;act.add(e.pointerId);viewport.setPointerCapture(e.pointerId);t0={x:e.clientX,y:e.clientY,t:e.target};if(act.size>1){stroke=pan=null;return}
if(tool==='draw'){const p=pt(e.clientX,e.clientY);checkpoint();if(ink.erase){eraseAt(p);stroke='erase'}else{stroke={color:ink.color,width:ink.width,points:[p]};state.strokes.push(stroke);drawAll()}return}
pan={x:e.clientX,y:e.clientY};viewport.classList.add('dragging')});
viewport.addEventListener('pointermove',e=>{if(act.size!==1||!act.has(e.pointerId))return;const p=pt(e.clientX,e.clientY);
if(stroke==='erase')return eraseAt(p);if(stroke){const a=stroke.points[stroke.points.length-1];stroke.points.push(p);ctx.strokeStyle=stroke.color;ctx.lineWidth=stroke.width;ctx.beginPath();ctx.moveTo(a.x,a.y);ctx.lineTo(p.x,p.y);ctx.stroke();return}
if(pan){view.x+=e.clientX-pan.x;view.y+=e.clientY-pan.y;pan={x:e.clientX,y:e.clientY};setView()}});
function endPtr(e){act.delete(e.pointerId);if(act.size)return;const moved=t0&&Math.hypot(e.clientX-t0.x,e.clientY-t0.y)>5;if(stroke&&stroke!=='erase')queueSave();
if(!moved&&tool!=='draw'&&t0){const l=t0.t.dataset&&t0.t.dataset.l;if(l!==undefined&&l!==''){if(confirm('حذف هذا الرابط؟')){checkpoint();state.links.splice(+l,1);renderLinks();queueSave()}}else{document.activeElement&&document.activeElement.blur&&document.activeElement.blur();cfrom=null;select(null)}}
stroke=pan=null;viewport.classList.remove('dragging')}
viewport.addEventListener('pointerup',endPtr);viewport.addEventListener('pointercancel',endPtr);
viewport.addEventListener('wheel',e=>{e.preventDefault();zoomAt(e.deltaY<0?1.1:.9,e.clientX,e.clientY)},{passive:false});
let pd=0,pm=null;const tp=e=>{const[a,b]=e.touches;return{d:Math.hypot(a.clientX-b.clientX,a.clientY-b.clientY),x:(a.clientX+b.clientX)/2,y:(a.clientY+b.clientY)/2}};
viewport.addEventListener('touchstart',e=>{if(e.touches.length===2){pm=tp(e);pd=pm.d}},{passive:true});
viewport.addEventListener('touchmove',e=>{if(e.touches.length!==2||!pd)return;const g=tp(e);zoomAt(g.d/pd,g.x,g.y,true);view.x+=g.x-pm.x;view.y+=g.y-pm.y;pd=g.d;pm=g;setView()},{passive:true});
viewport.addEventListener('touchend',()=>{pd=0});
/* ===== استيراد وتصدير ===== */
function download(blob,name){const u=URL.createObjectURL(blob),a=document.createElement('a');a.href=u;a.download=name;document.body.append(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(u),2500)}
const mark=()=>{try{localStorage.setItem('is-backup',Date.now())}catch(_){}};
function backup(){download(new Blob([JSON.stringify({app:'IdeaSpace',version:4,...state,view})],{type:'application/json'}),'ideaspace-backup.json');mark();toast('تم تنزيل النسخة الاحتياطية (تشمل الصور)')}
function importProject(f){if(!f)return;const r=new FileReader();r.onload=()=>{try{const d=JSON.parse(r.result);if(!d||!Array.isArray(d.items))throw 0;const m=migrate(d);checkpoint();restore(m);if(d.view&&isFinite(d.view.scale))view=d.view;setView();toast('تم استيراد المشروع')}catch(e){toast('الملف ليس مشروع IdeaSpace صالحاً — لم يتغيّر مشروعك')}};r.onerror=()=>toast('تعذّرت قراءة الملف');r.readAsText(f)}
function addImage(f){if(!f)return;if(!f.type.startsWith('image/'))return toast('اختر صورة. للملفات النصية استخدم «ملف نصي»');const u=URL.createObjectURL(f),im=new Image();im.onload=()=>{const k=Math.min(1,1600/Math.max(im.width,im.height)),c=document.createElement('canvas');c.width=Math.round(im.width*k);c.height=Math.round(im.height*k);c.getContext('2d').drawImage(im,0,0,c.width,c.height);URL.revokeObjectURL(u);checkpoint();const p=center(),o=add('image',p.x,p.y,{src:c.toDataURL('image/webp',.85),w:Math.min(300,c.width)});select(o.id);toast('أُضيفت الصورة (خُفِّض حجمها لتوفير الذاكرة)')};im.onerror=()=>toast('تعذّر فتح الصورة');im.src=u}
function addText(f){if(!f)return;const r=new FileReader();r.onload=()=>{checkpoint();const p=center(),t=String(r.result);add('file',p.x,p.y,{name:f.name,text:t.slice(0,20000)});toast(t.length>20000?'قُصّ الملف إلى أول 20000 حرف':'أُضيف محتوى الملف كنص')};r.onerror=()=>toast('تعذّرت قراءة الملف');r.readAsText(f)}
const esc=s=>String(s).replace(/[&<>"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
function wrap(t,w,fs){const m=Math.max(4,Math.floor(w/(fs*.55))),out=[];String(t||'').split('\n').forEach(p=>{let l='';p.split(' ').forEach(x=>{if((l+' '+x).trim().length>m&&l){out.push(l);l=x}else l=(l+' '+x).trim()});out.push(l)});return out}
function sceneSvg(){const b=bounds();if(!b)return null;const m=40,x0=b.x0-m,y0=b.y0-m,W=b.x1-b.x0+2*m,H=b.y1-b.y0+2*m;
let s=`<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}" viewBox="${x0} ${y0} ${W} ${H}"><rect x="${x0}" y="${y0}" width="${W}" height="${H}" fill="#111827"/>`;
for(const l of state.links){const k=linkD(l);if(k)s+=`<path d="${k.d}" fill="none" stroke="${k.col}" stroke-width="${k.tree?4:3}" stroke-linecap="round"/>`}
for(const o of state.items){const r=rect(o),fs=o.kind==='text'?(o.size||28):o.kind==='mind'?17:o.kind==='file'?12:15,mid=o.kind==='shape'||o.kind==='mind',bx=`x="${r.x}" y="${r.y}" width="${r.w}" height="${r.h}"`;
if(o.kind==='image'){s+=`<image ${bx} href="${o.src}"/>`;continue}
if(o.kind==='note')s+=`<rect ${bx} rx="3" fill="${HEX[o.color]||HEX.yellow}"/>`;
if(o.kind==='mind')s+=`<rect ${bx} rx="${r.h/2}" fill="${HEX[o.color]||HEX.mint}"/>`;
if(o.kind==='file')s+=`<rect ${bx} rx="14" fill="#202c40" stroke="#40506b"/>`;
if(o.kind==='shape')s+=`<svg ${bx} viewBox="0 0 100 100" preserveAspectRatio="none">${shapeBody(o)}</svg>`;
const col=o.kind==='text'||o.kind==='file'?'#f4f6fb':o.kind==='note'?'#2b2512':'#1b1830',ls=wrap((o.kind==='file'?'📄 '+(o.name||'')+'\n':'')+(o.text||''),r.w-24,fs),lh=fs*1.6,y=mid?r.y+r.h/2-(ls.length-1)*lh/2+fs*.35:r.y+14+fs;
s+=`<g font-family="system-ui,sans-serif" font-size="${fs}" font-weight="${o.kind==='text'||mid?700:400}" fill="${col}" direction="rtl">`+ls.map((t,i)=>`<text x="${mid?r.x+r.w/2:r.x+r.w-12}" y="${y+i*lh}" text-anchor="${mid?'middle':'end'}">${esc(t)}</text>`).join('')+'</g>'}
for(const k of state.strokes)s+=`<polyline points="${k.points.map(p=>p.x.toFixed(1)+','+p.y.toFixed(1)).join(' ')}" fill="none" stroke="${k.color}" stroke-width="${k.width}" stroke-linecap="round" stroke-linejoin="round"/>`;
return s+'</svg>'}
function exportSvg(){const s=sceneSvg();if(!s)return toast('اللوحة فارغة');download(new Blob([s],{type:'image/svg+xml'}),'ideaspace-board.svg')}
function exportPng(){const s=sceneSvg();if(!s)return toast('اللوحة فارغة');const b=bounds(),W=b.x1-b.x0+80,H=b.y1-b.y0+80,k=Math.min(2,4096/Math.max(W,H)),u=URL.createObjectURL(new Blob([s],{type:'image/svg+xml'})),im=new Image();im.onload=()=>{const c=document.createElement('canvas');c.width=Math.round(W*k);c.height=Math.round(H*k);c.getContext('2d').drawImage(im,0,0,c.width,c.height);URL.revokeObjectURL(u);c.toBlob(bl=>bl?download(bl,'ideaspace-board.png'):toast('تعذّر إنشاء PNG'))};im.onerror=()=>toast('تعذّر إنشاء PNG');im.src=u}
function newProject(){if(!confirm('بدء مشروع جديد؟ يمكنك التراجع، لكن صدّر نسخة احتياطية أولاً للاحتفاظ بالحالي.'))return;checkpoint();restore({});fit();toast('بدأ مشروع جديد')}
function command(c){$('#menu').classList.add('hidden');switch(c){
case'note':case'text':case'shape':case'mind':setTool('pan');create(c);break;
case'draw':setTool(tool==='draw'?'pan':'draw');if(tool==='draw')toast('وضع الرسم: ارسم بإصبعك. اضغط «تم» للخروج');break;
case'connect':setTool(tool==='connect'?'pan':'connect');if(tool==='connect')toast('اضغط عنصراً ثم العنصر الآخر');break;
case'image':$('#imageInput').click();break;case'file':$('#textInput').click();break;
case'undo':undo();break;case'redo':redo();break;case'backup':backup();break;case'restore':$('#projectInput').click();break;case'exportSvg':exportSvg();break;case'exportPng':exportPng();break;
case'zoomIn':zoomAt(1.2);break;case'zoomOut':zoomAt(1/1.2);break;case'reset':zoomAt(1/view.scale);break;case'fit':fit();break;case'new':newProject();break;
case'help':alert('IdeaSpace يحفظ مشروعك على هذا الجهاز فقط. مسح بيانات المتصفح قد يحذفه، فخذ نسخة JSON بانتظام.\nاضغط عنصراً لتحديده، واضغط عليه مرة أخرى لتحرير النص، واسحب المقبض لتغيير الحجم.');break}}
document.addEventListener('click',e=>{const b=e.target.closest('[data-cmd]');if(b){command(b.dataset.cmd);return}if(!e.target.closest('#menu')&&!e.target.closest('#menuBtn'))$('#menu').classList.add('hidden')});
$('#menuBtn').addEventListener('click',e=>{$('#menu').classList.toggle('hidden');e.stopPropagation()});
$('#imageInput').addEventListener('change',e=>{addImage(e.target.files[0]);e.target.value=''});$('#textInput').addEventListener('change',e=>{addText(e.target.files[0]);e.target.value=''});$('#projectInput').addEventListener('change',e=>{importProject(e.target.files[0]);e.target.value=''});
addEventListener('keydown',e=>{const a=document.activeElement,ed=a&&(a.isContentEditable||/INPUT|TEXTAREA/.test(a.tagName)),k=e.key.toLowerCase(),c=e.ctrlKey||e.metaKey;
if(k==='escape'){a&&a.blur&&a.blur();setTool('pan');select(null);return}if(ed)return;
if(c&&k==='z'){e.preventDefault();e.shiftKey?redo():undo()}else if(c&&k==='y'){e.preventDefault();redo()}
else if(c&&k==='d'&&sel){e.preventDefault();const b=props.querySelector('[data-p=dup]');b&&b.click()}
else if((k==='delete'||k==='backspace')&&sel){const o=get(sel);if(o&&!o.locked){checkpoint();del(o)}}});
addEventListener('resize',fitCanvas);
/* ===== الإقلاع ===== */
(async()=>{let d=null,legacy=false;try{d=await kv('readonly',s=>s.get('project'))}catch(e){toast('التخزين المحلي غير متاح في هذا المتصفح')}
if(!d){try{const raw=localStorage.getItem(LS);if(raw){d=JSON.parse(raw);legacy=true}}catch(_){}}
fitCanvas();restore(d||{});if(d&&d.view&&isFinite(d.view.scale)){view=d.view;setView()}else fit();
if(legacy)toast('نُقل مشروعك القديم إلى التخزين الجديد (النسخة القديمة لم تُحذف)');
else try{if(state.items.length&&Date.now()-(+localStorage.getItem('is-backup')||0)>6048e5)setTimeout(()=>toast('تذكير: خذ نسخة احتياطية JSON من القائمة ⋯'),2500)}catch(_){}
try{navigator.storage&&navigator.storage.persist&&navigator.storage.persist()}catch(_){}})();
if('serviceWorker'in navigator&&location.protocol.startsWith('http'))navigator.serviceWorker.register('./sw.js').catch(()=>{});
})();
