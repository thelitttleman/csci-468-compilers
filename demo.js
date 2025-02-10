
function* generator() {
    let i = 0;
    while (i < 10) {
        yield i
        i++
    }
}

let sequence = generator()
for (const num of sequence) {
    console.log(num)
}