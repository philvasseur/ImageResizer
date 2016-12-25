# ImageResizer
A content-aware image scaler similar to what exists in photoshop.

Takes two arguments for number of vertical seams
to remove and number of horizontal seams to remove. Works by calculating the
energy of every pixel. Energy is calculated based off of the surrounding pixels.
Then uses shortest path algorithm finding the least energetic path to get from
a top imaginary node to a bottom imaginary node. Uses an array of weightValues
which is the amount of energy needed to get to the point. Goes through all the
pixels and checks the 3 below it, seeing if going through that pixel would
be a shorter energy path. If so changes the energy needed to get to that pixel
and sets uses a fromNode array to keep track of where the path came from. 

Could definitely be improved in efficiency.
