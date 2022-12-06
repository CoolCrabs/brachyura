// run on https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.17
var out = "";
document.querySelectorAll(".screen").forEach(e => {
	if (/.*_attribute {.*/.test(e.innerHTML)) out += e.innerHTML;
});
console.log(out);
