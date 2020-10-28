function [output] = diffRand(n,r1,r2)
% diffRand(n,r):
% outputs a vector of size n of DIFFERENT random numbers that range from r1
% to r2... n cannot be larger than r.
output = zeros(1,n);
ctr = 1;
while output(n) == 0
    dummy = randi([r1,r2]);
    truth = 1; 
    for i = 1:ctr
        if dummy == output(i)
            truth = 0;
            break;
        end
    end
    if truth == 1
        output(ctr) = dummy;
        ctr = ctr + 1;
    end
end
end

