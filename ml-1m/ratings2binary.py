

def intToBytes(num):
    lst = [(num & (0xFF << 8*i)) >> 8*i for i in [3,2,1,0]]
    return ''.join(map(chr, lst))


data = open("ratings.dat")

outf = open("ratingsBin.dat", "wb")

count = 0

for line in data:
    if (count % 100000 == 0):
        print count
    nums = line.split("::")
    user = int(nums[0])
    movie = int(nums[1])
    rating = int(float(nums[2])*2)
    outf.write(intToBytes(user))
    outf.write(intToBytes(movie))
    outf.write(chr(rating))
    count += 1

outf.close()
data.close()
